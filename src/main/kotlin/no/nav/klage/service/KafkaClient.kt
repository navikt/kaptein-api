package no.nav.klage.service

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.time.Duration

object KafkaClient {

    private val logger = LoggerFactory.getLogger(KafkaClient::class.java.name)

    suspend fun startKafkaListener() {
        coroutineScope {
            launch { readFromTopic("klage.kaptein-behandlinger.v1") }
        }
        logger.debug("Method returning, but consumer will still be active in coroutine scope")
    }

    suspend fun readFromTopic(topic: String) {
        val consumerProps =
            mapOf(
                "bootstrap.servers" to "kafka.bootstrapServers",
                "auto.offset.reset" to "earliest",
                "key.deserializer" to "org.apache.kafka.common.serialization.StringDeserializer",
                "value.deserializer" to "org.apache.kafka.common.serialization.StringDeserializer",
                "group.id" to "someGroup",
                "security.protocol" to "PLAINTEXT"
            )

        val consumer = KafkaConsumer<String, String>(consumerProps)
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
}