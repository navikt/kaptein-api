package no.nav.klage.service

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import no.nav.klage.repository.BehandlingRepository
import org.slf4j.LoggerFactory

object KabalApiService {

    private val logger = LoggerFactory.getLogger(KabalApiService::class.java.name)

    suspend fun fetchAndStoreBehandlinger() {
        val client = HttpClient(CIO)

        val response = client.get("https://kabal-api/api/kaptein/behandlinger-stream") {
            // Add necessary headers, authentication, etc.
            header("Accept", "application/x-ndjson")

        }

        // Check if the response is successful and then stream the body
        if (response.status.isSuccess()) {
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(1024) // Read in chunks
                // TODO: Process
                logger.debug("Received chunk: ${packet.readText()}")
            }
        }

        BehandlingRepository.clearAndAddAll(emptyList())
    }

}