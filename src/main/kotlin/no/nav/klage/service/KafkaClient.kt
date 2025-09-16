package no.nav.klage.service

import io.ktor.util.logging.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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

    suspend fun startKafkaListener(): Job {
        logger.debug("Starting Kafka listener")
        val scope = coroutineScope {
            launch { readFromTopic("klage.kaptein-behandling.v1") }
        }
        return scope
    }

    suspend fun readFromTopic(topic: String) {
        val consumer = KafkaConsumer<String, String>(consumerConfig())
        coroutineScope {
            consumer.subscribe(listOf(topic))
            while (true) {
                logger.debug("Polling for messages from topic: $topic")
                val records = consumer.poll(Duration.ofSeconds(10))
                for (record in records) {
                    logger.debug("Received message: key=${record.key()}, offset=${record.offset()}")
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