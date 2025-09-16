package no.nav.klage.web

import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.http.*
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
            call.respond(getBehandlingListLedige())
        }

        get("/behandlinger/tildelte", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.respond(getBehandlingListTildelte())
        }

        get("/behandlinger/ferdigstilte", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerFinishedResponseView>()
                }
            }
        }) {
            call.respond(getBehandlingListFerdigstilte())
        }

        get("/internal/health") {
            call.respondText("OK")
        }

        get("/internal/isstarted") {
            if (BehandlingRepository.isReady()) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.ServiceUnavailable)
            }
        }
    }
}
