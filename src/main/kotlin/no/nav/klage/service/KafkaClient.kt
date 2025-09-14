package no.nav.klage.service

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.UUID

object KafkaClient {

    private val logger = LoggerFactory.getLogger(KafkaClient::class.java.name)

    suspend fun startKafkaListener() {
        coroutineScope {
            launch { readFromTopic("klage.kaptein-behandlinger.v1") }
        }
        logger.debug("Method returning, but consumer will still be active in coroutine scope")
    }

    suspend fun readFromTopic(topic: String) {

        val consumer = KafkaConsumer<String, String>(consumerConfig())
        coroutineScope {
            consumer.subscribe(listOf(topic))
            while (true) {
                logger.debug("Polling for messages from topic: $topic")
                val records = consumer.poll(Duration.ofSeconds(10))
                for (record in records) {
                    logger.info("Received message: key=${record.key()}, value=${record.value()}, topic=${record.topic()}, partition=${record.partition()}, offset=${record.offset()}")
                }
                consumer.commitSync()
            }
        }
    }

    private fun consumerConfig() = mapOf(
        BOOTSTRAP_SERVERS_CONFIG to System.getenv("KAFKA_BROKERS"),
        AUTO_OFFSET_RESET_CONFIG to "earliest",
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