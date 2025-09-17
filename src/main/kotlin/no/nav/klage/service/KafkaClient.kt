package no.nav.klage.service

import io.ktor.util.logging.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import no.nav.klage.domain.Behandling
import no.nav.klage.oppgave.util.ourJacksonObjectMapper
import no.nav.klage.repository.BehandlingRepository
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*

object KafkaClient {

    private val logger = KtorSimpleLogger(KafkaClient::class.java.name)

    suspend fun startKafkaListener() {
        coroutineScope {
            launch { readFromTopic("klage.kaptein-behandling.v1") }
        }
        logger.debug("Kafka listener started")
    }

    suspend fun readFromTopic(topic: String) {
        logger.debug("Starting Kafka listener for topic $topic")
        val consumer = KafkaConsumer<String, String>(consumerConfig())
        coroutineScope {
            consumer.subscribe(listOf(topic))
            while (true) {
                val records = consumer.poll(Duration.ofSeconds(10))
                for (record in records) {
                    logger.debug("Received message: key=${record.key()}, offset=${record.offset()}")
                    BehandlingRepository.addBehandling(
                        ourJacksonObjectMapper().readValue(
                            record.value(),
                            Behandling::class.java
                        )
                    )
                }
                consumer.commitSync()
            }
        }
    }

    private fun consumerConfig() = mapOf(
        BOOTSTRAP_SERVERS_CONFIG to System.getenv("KAFKA_BROKERS"),
        AUTO_OFFSET_RESET_CONFIG to "latest",
        KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        GROUP_ID_CONFIG to "kaptein-api-consumer_" + UUID.randomUUID().toString(),
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