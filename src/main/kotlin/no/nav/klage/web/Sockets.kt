package no.nav.klage.web

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import no.nav.klage.oppgave.util.ourJacksonObjectMapper
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/graphs-ws") {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()

                    val request = ourJacksonObjectMapper().readValue<GraphRequest>(text)

                    outgoing.send(
                        Frame.Text(
                            ourJacksonObjectMapper().writeValueAsString(
                                listOf(
                                    GraphResponse(name = "graph 1", value = (0..10).random()),
                                ),
                            ),
                        ),
                    )
                    if (text.equals(other = "bye", ignoreCase = true)) {
                        close(CloseReason(code = CloseReason.Codes.NORMAL, message = "Client said BYE"))
                    }
                }
            }
        }
    }
}

data class GraphRequest(
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val ytelser: List<String>,
    val klageenheter: List<String>,
    val sakstyper: List<String>,
    val innsendingshjemler: List<String>,
    val isTildelt: Boolean?,
)

data class GraphResponse(
    val name: String,
    val value: Int,
)
