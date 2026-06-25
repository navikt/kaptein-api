package no.nav.klage.service

import io.ktor.util.logging.*
import no.nav.klage.domain.Behandling
import no.nav.klage.oppgave.util.ourJacksonObjectMapper
import no.nav.klage.repository.BehandlingRepository
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

object KafkaClient {

    private val logger = KtorSimpleLogger(KafkaClient::class.java.name)
    private val isRunning = AtomicBoolean(true)
    @Volatile private var consumer: KafkaConsumer<String, String>? = null

    private const val INITIAL_BACKOFF_MS = 1_000L
    private const val MAX_BACKOFF_MS = 60_000L

    // If the consumer has not polled within this window we consider it unhealthy.
    // A healthy consumer polls roughly every second (poll timeout is 1s), even when idle.
    private const val STALE_POLL_THRESHOLD_SECONDS = 120L

    // --- Diagnostics state (written only inside the poll loop / restart wrapper, read from HTTP threads) ---
    private val groupId: String = "kaptein-api-consumer_" + UUID.randomUUID().toString()
    @Volatile private var consumerAlive: Boolean = false
    @Volatile private var lastPollEpochMs: Long = 0
    @Volatile private var lastRecordEpochMs: Long = 0
    @Volatile private var processedCount: Long = 0
    @Volatile private var skippedCount: Long = 0
    @Volatile private var restartCount: Long = 0
    @Volatile private var lastRestartEpochMs: Long = 0
    @Volatile private var lastError: String? = null
    @Volatile private var lastErrorEpochMs: Long = 0
    @Volatile private var partitionPositions: Map<String, Long> = emptyMap()

    data class KafkaDiagnostics(
        val groupId: String,
        val consumerAlive: Boolean,
        val isRunningFlag: Boolean,
        val healthy: Boolean,
        val lastPollEpochMs: Long,
        val secondsSinceLastPoll: Long,
        val lastRecordEpochMs: Long,
        val secondsSinceLastRecord: Long,
        val processedCount: Long,
        // messages that could not be processed and were skipped (poison messages); these
        // represent updates this instance did NOT apply and are a possible source of drift
        val skippedCount: Long,
        // how many times the consumer loop has been restarted after a failure
        val restartCount: Long,
        val lastRestartEpochMs: Long,
        val secondsSinceLastRestart: Long,
        val partitionPositions: Map<String, Long>,
        val lastError: String?,
        val lastErrorEpochMs: Long,
    )

    fun getDiagnostics(): KafkaDiagnostics {
        val now = System.currentTimeMillis()
        return KafkaDiagnostics(
            groupId = groupId,
            consumerAlive = consumerAlive,
            isRunningFlag = isRunning.get(),
            healthy = isConsumerHealthy(),
            lastPollEpochMs = lastPollEpochMs,
            secondsSinceLastPoll = if (lastPollEpochMs == 0L) -1 else (now - lastPollEpochMs) / 1000,
            lastRecordEpochMs = lastRecordEpochMs,
            secondsSinceLastRecord = if (lastRecordEpochMs == 0L) -1 else (now - lastRecordEpochMs) / 1000,
            processedCount = processedCount,
            skippedCount = skippedCount,
            restartCount = restartCount,
            lastRestartEpochMs = lastRestartEpochMs,
            secondsSinceLastRestart = if (lastRestartEpochMs == 0L) -1 else (now - lastRestartEpochMs) / 1000,
            partitionPositions = partitionPositions,
            lastError = lastError,
            lastErrorEpochMs = lastErrorEpochMs,
        )
    }

    /**
     * Consumer is healthy if it is supposed to be running and is actively polling.
     * - During shutdown (isRunning=false) we report healthy so we don't trigger spurious restarts.
     * - Before the very first poll (lastPollEpochMs==0) we report healthy as a startup grace period;
     *   the startup probe (/internal/isstarted) gates traffic until the initial load is done.
     * - Otherwise we require a recent poll. This naturally covers a dead loop, a stuck poll, and
     *   flapping with long gaps. Note: it does NOT clear on restartCount alone, because a consumer
     *   that restarts quickly and keeps polling is still serving fresh data.
     */
    fun isConsumerHealthy(): Boolean {
        if (!isRunning.get()) return true
        if (lastPollEpochMs == 0L) return true
        val secondsSinceLastPoll = (System.currentTimeMillis() - lastPollEpochMs) / 1000
        return secondsSinceLastPoll < STALE_POLL_THRESHOLD_SECONDS
    }

    fun startKafkaListener() {
        runConsumerWithRestart("klage.kaptein-behandling.v1")
    }

    fun stopKafkaListener() {
        logger.debug("Stopping Kafka listener...")
        isRunning.set(false)
        consumer?.wakeup()
    }

    /**
     * Supervises the consumer: if the poll loop dies because of an infrastructure error
     * (broker disconnect, rebalance error, etc.) we recover by recreating the consumer and
     * resuming. Recovery is made observable rather than hidden:
     *  - restartCount / lastRestartEpochMs / lastError are preserved and exposed via diagnostics,
     *  - every restart is logged at WARN/ERROR,
     *  - the group id is stable for this process, so a restart resumes from committed offsets
     *    instead of jumping to 'latest' and silently skipping messages produced while it was down.
     */
    private fun runConsumerWithRestart(topic: String) {
        var backoffMs = INITIAL_BACKOFF_MS
        while (isRunning.get()) {
            val loopStart = System.currentTimeMillis()
            try {
                readFromTopic(topic)
                // readFromTopic returns normally only on graceful shutdown
                return
            } catch (e: Exception) {
                if (!isRunning.get()) {
                    logger.debug("Consumer stopped during shutdown")
                    return
                }
                restartCount++
                lastRestartEpochMs = System.currentTimeMillis()
                lastError = "consumer loop failed: ${e.message}"
                lastErrorEpochMs = lastRestartEpochMs
                logger.error("Kafka consumer loop failed (restart #$restartCount). Will recreate the consumer and resume from committed offsets after ${backoffMs}ms.", e)
            }

            if (!isRunning.get()) return
            try {
                Thread.sleep(backoffMs)
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt()
                return
            }
            // Reset backoff if the previous run was healthy for a while, otherwise grow it.
            backoffMs = if (System.currentTimeMillis() - loopStart > 60_000) {
                INITIAL_BACKOFF_MS
            } else {
                min(backoffMs * 2, MAX_BACKOFF_MS)
            }
        }
    }

    private fun readFromTopic(topic: String) {
        logger.debug("Starting Kafka listener for topic $topic")
        consumer = KafkaConsumer(consumerConfig())
        try {
            consumer!!.subscribe(listOf(topic))
            consumerAlive = true
            while (isRunning.get()) {
                val records = consumer!!.poll(Duration.ofSeconds(2))
                lastPollEpochMs = System.currentTimeMillis()
                for (record in records) {
                    try {
                        logger.debug("Received message: key=${record.key()}, offset=${record.offset()}")
                        BehandlingRepository.addBehandling(
                            ourJacksonObjectMapper().readValue(
                                record.value(),
                                Behandling::class.java
                            )
                        )
                        processedCount++
                        lastRecordEpochMs = System.currentTimeMillis()
                    } catch (e: Exception) {
                        // Poison message: skip it so the loop keeps running and we don't get stuck
                        // re-failing on the same offset forever. This update is NOT applied on this
                        // instance, so it is a possible source of drift -> make it loud and counted.
                        skippedCount++
                        lastError = "skipped poison message offset=${record.offset()} key=${record.key()}: ${e.message}"
                        lastErrorEpochMs = System.currentTimeMillis()
                        logger.error("Skipping unprocessable message (key=${record.key()}, offset=${record.offset()}). This update will not be applied on this instance, which may cause drift between instances.", e)
                    }
                }
                // record current positions per partition for drift diagnostics
                partitionPositions = consumer!!.assignment().associate { tp ->
                    "${tp.topic()}-${tp.partition()}" to consumer!!.position(tp)
                }
                consumer!!.commitSync()
            }
        } catch (e: WakeupException) {
            if (isRunning.get()) {
                throw e
            }
            logger.debug("Kafka consumer wakeup received during shutdown")
        } finally {
            consumerAlive = false
            logger.warn("Closing Kafka consumer (isRunning=${isRunning.get()}).")
            consumer?.close()
            logger.debug("Kafka consumer closed")
        }
    }

    private fun consumerConfig() = mapOf(
        BOOTSTRAP_SERVERS_CONFIG to System.getenv("KAFKA_BROKERS"),
        AUTO_OFFSET_RESET_CONFIG to "latest",
        ENABLE_AUTO_COMMIT_CONFIG to false,
        KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        GROUP_ID_CONFIG to groupId,
    ) + securityConfig()

    private fun securityConfig() = mapOf(
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SSL",
        SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to "", // Disable server host name verification
        SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to "JKS",
        SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to "PKCS12",
        SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to System.getenv("KAFKA_TRUSTSTORE_PATH"),
        SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to System.getenv("KAFKA_CREDSTORE_PASSWORD"),
        SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to System.getenv("KAFKA_KEYSTORE_PATH"),
        SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to System.getenv("KAFKA_CREDSTORE_PASSWORD"),
        SslConfigs.SSL_KEY_PASSWORD_CONFIG to System.getenv("KAFKA_CREDSTORE_PASSWORD"),
    )
}