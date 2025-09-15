package no.nav.klage.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.utils.io.*
import no.nav.klage.domain.Behandling
import no.nav.klage.oppgave.util.ourJacksonObjectMapper
import no.nav.klage.repository.BehandlingRepository
import org.slf4j.LoggerFactory

object KabalApiService {

    private val logger = LoggerFactory.getLogger(KabalApiService::class.java.name)

    suspend fun fetchAndStoreBehandlinger() {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                jackson()
            }
        }

        val tokenEndpoint = System.getenv("NAIS_TOKEN_ENDPOINT")
        val cluster = System.getenv("NAIS_CLUSTER_NAME")
        val target = "api://$cluster.klage.kabal-api/.default"

        val tokenResponse = client.post(tokenEndpoint) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(
                TokenRequest(
                    identity_provider = "azuread",
                    target = target,
                ),
            )
        }.body<TokenResponse>()

        logger.debug("About to fetch behandlinger from Kabal API")
        val response = client.get("http://kabal-api/api/kaptein/behandlinger-stream") {
            contentType(ContentType.Application.Json)
            header("Accept", "application/x-ndjson")
            header("Authorization", "Bearer ${tokenResponse.access_token}")
        }

        val behandlingList = mutableListOf<Behandling>()

        try {
            // Check if the response is successful and then stream the body
            if (response.status.isSuccess()) {
                logger.debug("Response status is successful: {}", response.status)
                println("Response status: ${response.status}")
                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    logger.debug("Reading line from stream")
                    val behandlingAsString = channel.readUTF8Line()
                    logger.debug("Received line: $behandlingAsString")
                    println(behandlingAsString)
                    if (!behandlingAsString.isNullOrBlank()) {
                        behandlingList += ourJacksonObjectMapper().readValue(behandlingAsString, Behandling::class.java)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error while fetching or processing behandlinger: ", e)
            println("Error while fetching or processing behandlinger: ${e.message}")
            e.printStackTrace()
        }

        BehandlingRepository.clearAndAddAll(behandlingList)
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