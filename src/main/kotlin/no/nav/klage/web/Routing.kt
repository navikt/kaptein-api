package no.nav.klage.web

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import no.nav.klage.repository.BehandlingRepository
import no.nav.klage.service.GraphCreator

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Application started")
        }

        get("/internal/health") {
            call.respondText("OK")
        }

        get("/internal/isready") {
            if (BehandlingRepository.isReady()) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.ServiceUnavailable)
            }
        }

        get("/active/graphs") {
            launch {
                val v = GraphCreator.calculateGraphs()

                call.respond(GraphResponse(
                    name = "Sample Graph",
                    value = 10,
                ))
            }
        }

        get("/finished/graphs") {
            launch {
                val v = GraphCreator.calculateGraphs()

                call.respond(GraphResponse(
                    name = "Sample Graph",
                    value = 10,
                ))
            }
        }
    }
}
