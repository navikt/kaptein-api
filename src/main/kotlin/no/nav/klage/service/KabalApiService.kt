package no.nav.klage.service

import io.ktor.client.*
import io.ktor.client.call.*
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

        val tokenEndpoint = System.getenv("NAIS_TOKEN_ENDPOINT")
        val cluster = System.getenv("NAIS_CLUSTER_NAME")
        val target = "api://$cluster.klage.kabal-api/.default"

        val tokenResponse = client.post(tokenEndpoint) {
            header("Content-Type", "application/json")
            setBody(
                TokenRequest(
                    identity_provider = "azuread",
                    target = target,
                ),
            )
        }.body<TokenResponse>()

        val response = client.get("http://kabal-api/api/kaptein/behandlinger-stream") {
            // Add necessary headers, authentication, etc.
            header("Accept", "application/x-ndjson")
            header("Authorization", "Bearer ${tokenResponse.access_token}")
        }

        // Check if the response is successful and then stream the body
        if (response.status.isSuccess()) {
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(1024) // Read in chunks
                // TODO: Process
                val readText = packet.readText()
                logger.debug("Received chunk: $readText")
                println(readText)
            }
        }

        BehandlingRepository.clearAndAddAll(emptyList())
    }
}

data class TokenRequest(
    val identity_provider: String,
    val target: String,
)

data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
)