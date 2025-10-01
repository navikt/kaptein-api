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
import no.nav.klage.domain.TRBehandlingerActiveResponseView
import no.nav.klage.domain.TRBehandlingerFinishedResponseView
import no.nav.klage.kodeverk.Type.*
import no.nav.klage.repository.BehandlingRepository
import no.nav.klage.service.*
import no.nav.klage.util.validateToken

fun Application.configureRouting() {
    routing {
        route("/api.json") {
            openApi()
        }

        route("/swagger-ui") {
            swaggerUI("/api.json")
        }

        get("/klager/ledige", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListLedige(KLAGE))
        }

        get("/anker/ledige", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListLedige(ANKE))
        }

        get("/behandlinger-etter-tr-opphevet/ledige", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListLedige(BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET))
        }

        get("/omgjoeringskrav/ledige", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListLedige(OMGJOERINGSKRAV))
        }

        get("/anker-i-tr/ledige", {
            response {
                HttpStatusCode.OK to {
                    body<TRBehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getTRBehandlingListLedige())
        }

        get("/klager/tildelte", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListTildelte(KLAGE))
        }

        get("/anker/tildelte", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListTildelte(ANKE))
        }

        get("/behandlinger-etter-tr-opphevet/tildelte", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListTildelte(BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET))
        }

        get("/omgjoeringskrav/tildelte", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListTildelte(OMGJOERINGSKRAV))
        }

        get("/anker-i-tr/tildelte", {
            response {
                HttpStatusCode.OK to {
                    body<TRBehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getTRBehandlingListTildelte())
        }

        get("/klager/ferdigstilte", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerFinishedResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListFerdigstilte(KLAGE))
        }

        get("/anker/ferdigstilte", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerFinishedResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListFerdigstilte(ANKE))
        }

        get("/behandlinger-etter-tr-opphevet/ferdigstilte", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerFinishedResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListFerdigstilte(BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET))
        }

        get("/omgjoeringskrav/ferdigstilte", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerFinishedResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListFerdigstilte(OMGJOERINGSKRAV))
        }

        get("/anker-i-tr/ferdigstilte", {
            response {
                HttpStatusCode.OK to {
                    body<TRBehandlingerFinishedResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getTRBehandlingListFerdigstilte())
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