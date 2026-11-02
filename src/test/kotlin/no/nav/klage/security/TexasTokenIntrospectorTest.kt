package no.nav.klage.security

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TexasTokenIntrospectorTest {
    private fun introspector(
        responseBody: String,
        requests: MutableList<String> = mutableListOf(),
    ): TexasTokenIntrospector {
        val engine =
            MockEngine { request ->
                assertThat(request.method).isEqualTo(HttpMethod.Post)
                assertThat(request.url.toString()).isEqualTo("http://texas/introspect")
                requests += String(request.body.toByteArray())
                respond(content = responseBody, headers = headersOf(name = HttpHeaders.ContentType, value = "application/json"))
            }
        return TexasTokenIntrospector(
            introspectionEndpoint = "http://texas/introspect",
            httpClient = HttpClient(engine) { install(ContentNegotiation) { jackson() } },
        )
    }

    @Test
    fun `sends the token as an Azure token`() {
        runBlocking {
            val requests = mutableListOf<String>()
            introspector(responseBody = """{"active":false}""", requests = requests).introspect(token = "the-token")
            assertThat(requests).containsExactly("""{"identity_provider":"azuread","token":"the-token"}""")
        }
    }

    @Test
    fun `reads active and azp and ignores other claims`() {
        runBlocking {
            val result =
                introspector(responseBody = """{"active":true,"azp":"client-id","NAVident":"Z123456","groups":["a"]}""")
                    .introspect(token = "t")
            assertThat(result.active).isTrue()
            assertThat(result.azp).isEqualTo("client-id")
        }
    }

    @Test
    fun `reads error from inactive token`() {
        runBlocking {
            val result = introspector(responseBody = """{"active":false,"error":"token is expired"}""").introspect(token = "t")
            assertThat(result.active).isFalse()
            assertThat(result.error).isEqualTo("token is expired")
        }
    }
}
