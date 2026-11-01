package no.nav.klage.security

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer
import io.ktor.util.logging.KtorSimpleLogger

const val AZURE_AUTH = "azure"

private val logger = KtorSimpleLogger("no.nav.klage.security.Authentication")

/** The app that called us, identified by the `azp` claim (client id). */
data class AzureCaller(
    val clientId: String,
)

/**
 * Accepts Azure tokens that Texas reports as active and that come from an app in our inbound
 * access policy. Every other request on routes inside `authenticate(AZURE_AUTH)` gets 401.
 */
fun Application.configureAuthentication(
    tokenIntrospector: TokenIntrospector,
    preAuthorizedClientIds: Set<String>,
) {
    install(Authentication) {
        bearer(AZURE_AUTH) {
            authenticate { credential ->
                val result = tokenIntrospector.introspect(token = credential.token)
                when {
                    !result.active -> {
                        logger.warn("Token validation failed: {}", result.error)
                        null
                    }

                    result.azp == null || result.azp !in preAuthorizedClientIds -> {
                        logger.warn("Rejected token from caller that is not pre-authorized: {}", result.azp)
                        null
                    }

                    else -> {
                        AzureCaller(clientId = result.azp)
                    }
                }
            }
        }
    }
}

/** Client ids from Nais' AZURE_APP_PRE_AUTHORIZED_APPS, a JSON list of {name, clientId}. */
fun parsePreAuthorizedClientIds(json: String): Set<String> =
    jacksonObjectMapper()
        .readValue<List<PreAuthorizedApp>>(json)
        .map { it.clientId }
        .toSet()

@JsonIgnoreProperties(ignoreUnknown = true)
private data class PreAuthorizedApp(
    val clientId: String,
)
