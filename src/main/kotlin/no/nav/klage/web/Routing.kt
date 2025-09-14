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
            call.respond(HttpStatusCode.OK)
//            if (BehandlingRepository.isReady()) {
//                call.respond(HttpStatusCode.OK)
//            } else {
//                call.respond(HttpStatusCode.ServiceUnavailable)
//            }
        }

        //TODO add request params and the response
        get("/graphs") {
            val start = System.currentTimeMillis()
            val job = launch {
                GraphCreator.calculateGraphs()
            }
            job.join()
            val end = System.currentTimeMillis()
            call.respondText("Graphs done in ${end - start} ms")
        }
    }
}
