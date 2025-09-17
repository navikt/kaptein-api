package no.nav.klage

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration

@Disabled("just for local development testing")
@Testcontainers
class KafkaTest {

    companion object {
        @JvmStatic
        @Container
        val kafka = ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.8.3"))
    }

    @Test
    fun `test kafka connection`() {
        val producerProps = mapOf<String, String>(
            "bootstrap.servers" to kafka.bootstrapServers,
            "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
            "value.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
            "security.protocol" to "PLAINTEXT"
        )

        println(System.getenv())

        val producer = KafkaProducer<String, String>(producerProps)
        val r = ProducerRecord("test", "1", "Hello, world!")
        producer.send(r).get()

        val consumerProps =
            mapOf(
                "bootstrap.servers" to kafka.bootstrapServers,
                "auto.offset.reset" to "earliest",
                "key.deserializer" to "org.apache.kafka.common.serialization.StringDeserializer",
                "value.deserializer" to "org.apache.kafka.common.serialization.StringDeserializer",
                "group.id" to "someGroup",
                "security.protocol" to "PLAINTEXT"
            )

        val consumer = KafkaConsumer<String, String>(consumerProps)
        consumer.subscribe(listOf("test"))
        val records = consumer.poll(Duration.ofMillis(400))

        records.forEach { record ->
            println("Received message: ${record.value()} with key: ${record.key()}")
        }
    }

}