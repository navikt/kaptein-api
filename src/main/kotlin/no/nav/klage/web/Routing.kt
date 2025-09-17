package no.nav.klage.web

import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.klage.domain.BehandlingerActiveResponseView
import no.nav.klage.domain.BehandlingerFinishedResponseView
import no.nav.klage.repository.BehandlingRepository
import no.nav.klage.service.getBehandlingListFerdigstilte
import no.nav.klage.service.getBehandlingListLedige
import no.nav.klage.service.getBehandlingListTildelte

fun Application.configureRouting() {
    routing {
        route("/api.json") {
            openApi()
        }

        route("/swagger-ui") {
            swaggerUI("/api.json")
        }

        get("/behandlinger/ledige", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListLedige())
        }

        get("/behandlinger/tildelte", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListTildelte())
        }

        get("/behandlinger/ferdigstilte", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerFinishedResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListFerdigstilte())
        }

        get("/internal/health") {
            call.respondText("OK")
        }

        get("/internal/isstarted") {
            log
            if (BehandlingRepository.isReady()) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.ServiceUnavailable)
            }
        }
    }
}

suspend fun RoutingCall.validateToken() {
    val token = this.request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()
    if (token == null || token.isEmpty()) {
        this.application.log.warn("Missing or empty Authorization header")
        this.respond(HttpStatusCode.Unauthorized)
    } else {
        val tokenEndpoint = System.getenv("NAIS_TOKEN_INTROSPECTION_ENDPOINT")

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                jackson()
            }
        }

        val validateTokenResponse = client.post(tokenEndpoint) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(
                ValidateTokenRequest(
                    identity_provider = "azuread",
                    token = token,
                ),
            )
        }.body<ValidateTokenResponse>()

        if (validateTokenResponse.active) {
            return
        } else {
            this.application.log.warn("Token validation failed due to: {}", validateTokenResponse.error)
            this.respond(HttpStatusCode.Forbidden, validateTokenResponse.error ?: "Invalid token")
        }
    }
}

private data class ValidateTokenRequest(
    val identity_provider: String,
    val token: String,
)

private data class ValidateTokenResponse(
    val active: Boolean,
    val error: String?,
)