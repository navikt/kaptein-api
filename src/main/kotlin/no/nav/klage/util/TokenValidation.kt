package no.nav.klage.util

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall
import io.ktor.util.logging.KtorSimpleLogger

private val logger = KtorSimpleLogger("no.nav.klage.util.TokenValidation")

private val tokenValidationClient =
    HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
    }

suspend fun RoutingCall.validateToken() {
    val token =
        this.request.headers["Authorization"]
            ?.removePrefix("Bearer ")
            ?.trim()
    if (token == null || token.isEmpty()) {
        logger.warn("Missing or empty Authorization header")
        this.respond(HttpStatusCode.Unauthorized)
        return
    } else {
        val start = System.currentTimeMillis()
        val tokenEndpoint = System.getenv("NAIS_TOKEN_INTROSPECTION_ENDPOINT")

        val validateTokenResponse =
            tokenValidationClient
                .post(tokenEndpoint) {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(
                        ValidateTokenRequest(
                            identity_provider = "azuread",
                            token = token,
                        ),
                    )
                }.body<ValidateTokenResponse>()
        logger.debug("Validate token took ${System.currentTimeMillis() - start} ms")

        if (validateTokenResponse.active) {
            return
        } else {
            logger.warn("Token validation failed due to: {}", validateTokenResponse.error)
            this.respond(
                status = HttpStatusCode.Forbidden,
                message = validateTokenResponse.error ?: "Invalid token",
            )
        }
    }
}

private data class ValidateTokenRequest(
    val identity_provider: String,
    val token: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class ValidateTokenResponse(
    val active: Boolean,
    val error: String?,
)
