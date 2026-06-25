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
            call.respond(getTRBehandlingListLedige(type = ANKE_I_TRYGDERETTEN))
        }

        get("/begjaeringer-om-gjenopptak/ledige", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListLedige(type = BEGJAERING_OM_GJENOPPTAK))
        }

        get("/begjaeringer-om-gjenopptak-i-tr/ledige", {
            response {
                HttpStatusCode.OK to {
                    body<TRBehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getTRBehandlingListLedige(type = BEGJAERING_OM_GJENOPPTAK_I_TRYGDERETTEN))
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
            call.respond(getTRBehandlingListTildelte(type = ANKE_I_TRYGDERETTEN))
        }

        get("/begjaeringer-om-gjenopptak/tildelte", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListTildelte(type = BEGJAERING_OM_GJENOPPTAK))
        }

        get("/begjaeringer-om-gjenopptak-i-tr/tildelte", {
            response {
                HttpStatusCode.OK to {
                    body<TRBehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getTRBehandlingListTildelte(type = BEGJAERING_OM_GJENOPPTAK_I_TRYGDERETTEN))
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
            call.respond(getTRBehandlingListFerdigstilte(type = ANKE_I_TRYGDERETTEN))
        }

        get("/begjaeringer-om-gjenopptak/ferdigstilte", {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerFinishedResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListFerdigstilte(type = BEGJAERING_OM_GJENOPPTAK))
        }

        get("/begjaeringer-om-gjenopptak-i-tr/ferdigstilte", {
            response {
                HttpStatusCode.OK to {
                    body<TRBehandlingerFinishedResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getTRBehandlingListFerdigstilte(type = BEGJAERING_OM_GJENOPPTAK_I_TRYGDERETTEN))
        }

        get("/internal/health") {
            // Liveness + readiness both point here. Fail it when the Kafka consumer is no longer
            // polling: a stuck/dead consumer means this pod serves stale data and has drifted, so
            // we want it pulled from the load balancer and restarted to re-bootstrap.
            if (KafkaClient.isConsumerHealthy()) {
                call.respondText("OK")
            } else {
                call.respond(HttpStatusCode.ServiceUnavailable, "Kafka consumer unhealthy")
            }
        }

        get("/internal/isstarted") {
            if (BehandlingRepository.isReady()) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.ServiceUnavailable)
            }
        }

        // Diagnostics for cross-instance drift. Call this directly on each pod IP
        // (bypassing the Service load balancer) and compare 'fingerprint' and 'kafka'.
        // Same count+checksum => instances are in sync. consumerAlive=false or a large
        // secondsSinceLastPoll/lastError on one pod explains divergence.
        get("/internal/diagnostics") {
            call.respond(
                mapOf(
                    "ready" to BehandlingRepository.isReady(),
                    "fingerprint" to BehandlingRepository.getStateFingerprint(),
                    "kafka" to KafkaClient.getDiagnostics(),
                )
            )
        }
    }
}