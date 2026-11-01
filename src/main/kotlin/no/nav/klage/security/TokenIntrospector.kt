package no.nav.klage.security

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson

fun interface TokenIntrospector {
    suspend fun introspect(token: String): IntrospectionResult
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class IntrospectionResult(
    val active: Boolean,
    val azp: String? = null,
    val error: String? = null,
)

/**
 * Validates Azure tokens with Texas (signature, issuer, audience and expiry).
 * https://doc.nais.io/auth/explanations/#texas
 */
class TexasTokenIntrospector(
    private val introspectionEndpoint: String,
    private val httpClient: HttpClient = defaultHttpClient(),
) : TokenIntrospector {
    override suspend fun introspect(token: String): IntrospectionResult =
        httpClient
            .post(introspectionEndpoint) {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(IntrospectionRequest(identity_provider = "azuread", token = token))
            }.body()

    companion object {
        fun defaultHttpClient(): HttpClient =
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    jackson()
                }
            }
    }
}

@Suppress("ConstructorParameterNaming")
private data class IntrospectionRequest(
    val identity_provider: String,
    val token: String,
)
