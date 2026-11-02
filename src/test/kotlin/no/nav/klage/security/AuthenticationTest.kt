package no.nav.klage.security

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import no.nav.klage.web.configureRouting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AuthenticationTest {
    companion object {
        private const val PROTECTED_PATH = "/klager/ledige"
        private const val KAPTEIN_CLIENT_ID = "kaptein-client-id"
        private const val VALID_TOKEN = "valid"
        private const val TOKEN_FROM_OTHER_APP = "other-app"
    }

    private val introspectedTokens = mutableListOf<String>()

    private val fakeTexas =
        TokenIntrospector { token ->
            introspectedTokens += token
            when (token) {
                VALID_TOKEN -> IntrospectionResult(active = true, azp = KAPTEIN_CLIENT_ID)
                TOKEN_FROM_OTHER_APP -> IntrospectionResult(active = true, azp = "unknown-client")
                else -> IntrospectionResult(active = false, error = "token is expired")
            }
        }

    private fun authTest(block: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            application {
                install(ContentNegotiation) { jackson { registerModule(JavaTimeModule()) } }
                configureAuthentication(tokenIntrospector = fakeTexas, preAuthorizedClientIds = setOf(KAPTEIN_CLIENT_ID))
                configureRouting()
            }
            block()
        }

    @Test
    fun `accepts active token from pre-authorized caller`() =
        authTest {
            val response = client.get(PROTECTED_PATH) { header(key = HttpHeaders.Authorization, value = "Bearer $VALID_TOKEN") }
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        }

    @Test
    fun `rejects request without token without calling Texas`() =
        authTest {
            val response = client.get(PROTECTED_PATH)
            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
            assertThat(response.headers[HttpHeaders.WWWAuthenticate]).startsWith("Bearer")
            assertThat(introspectedTokens).isEmpty()
        }

    @Test
    fun `rejects token Texas reports as inactive`() =
        authTest {
            val response = client.get(PROTECTED_PATH) { header(key = HttpHeaders.Authorization, value = "Bearer expired") }
            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
            assertThat(introspectedTokens).containsExactly("expired")
        }

    @Test
    fun `rejects token from caller that is not pre-authorized`() =
        authTest {
            val response = client.get(PROTECTED_PATH) { header(key = HttpHeaders.Authorization, value = "Bearer $TOKEN_FROM_OTHER_APP") }
            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }

    @Test
    fun `rejects other authorization schemes`() =
        authTest {
            val response = client.get(PROTECTED_PATH) { header(key = HttpHeaders.Authorization, value = "Basic $VALID_TOKEN") }
            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
            assertThat(introspectedTokens).isEmpty()
        }

    @Test
    fun `health endpoints are reachable without token`() =
        authTest {
            assertThat(client.get("/internal/health").status).isNotEqualTo(HttpStatusCode.Unauthorized)
            assertThat(client.get("/internal/isstarted").status).isNotEqualTo(HttpStatusCode.Unauthorized)
        }

    @Test
    fun `parses client ids from AZURE_APP_PRE_AUTHORIZED_APPS`() {
        val json = """[{"name":"dev-gcp:klage:kaptein","clientId":"id-1"},{"name":"dev-gcp:klage:other","clientId":"id-2"}]"""
        assertThat(parsePreAuthorizedClientIds(json = json)).containsExactlyInAnyOrder("id-1", "id-2")
    }
}
