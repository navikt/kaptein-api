package no.nav.klage.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.jackson.jackson
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.utils.io.readLine
import no.nav.klage.domain.Behandling
import no.nav.klage.oppgave.util.ourJacksonObjectMapper
import no.nav.klage.repository.BehandlingRepository

object KabalApiService {
    private val logger = KtorSimpleLogger(KabalApiService::class.java.name)

    suspend fun fetchAndStoreBehandlinger() {
        val start = System.currentTimeMillis()
        logger.debug("fetchAndStoreBehandlinger")
        val client =
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    jackson()
                }
            }

        val tokenEndpoint = System.getenv("NAIS_TOKEN_ENDPOINT")
        val cluster = System.getenv("NAIS_CLUSTER_NAME")
        val target = "api://$cluster.klage.kabal-api/.default"

        val acquireTokenResponse =
            client
                .post(tokenEndpoint) {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(
                        AcquireTokenRequest(
                            identity_provider = "azuread",
                            target = target,
                        ),
                    )
                }.body<AcquireTokenResponse>()

        var counter = 0

        logger.debug("About to fetch behandlinger from Kabal API")
        val response =
            client.get("http://kabal-api/api/kaptein/behandlinger-stream") {
                timeout {
                    requestTimeoutMillis = 1000 * 60 * 5
                }
                contentType(ContentType.Application.Json)
                header(key = "Accept", value = "application/x-ndjson")
                header(key = "Authorization", value = "Bearer ${acquireTokenResponse.access_token}")
            }

        val ourJacksonObjectMapper = ourJacksonObjectMapper()

        try {
            // Check if the response is successful and then stream the body
            if (response.status.isSuccess()) {
                logger.debug("Response status is successful: {}", response.status)
                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val behandlingAsString = channel.readLine()
                    if (!behandlingAsString.isNullOrBlank()) {
                        BehandlingRepository.addBehandling(
                            ourJacksonObjectMapper.readValue(
                                behandlingAsString,
                                Behandling::class.java,
                            ),
                        )
                    }
                    if (++counter % 1000 == 0) {
                        logger.debug("Fetched $counter behandlinger so far...")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error while fetching or processing behandlinger: ", e)
        }

        client.close()

        logger.debug(
            "Fetched total of $counter behandlinger in ${System.currentTimeMillis() - start} ms. Setting application as ready (k8s).",
        )
        BehandlingRepository.setReady()
    }
}

private data class AcquireTokenRequest(
    val identity_provider: String,
    val target: String,
)

private data class AcquireTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
)
