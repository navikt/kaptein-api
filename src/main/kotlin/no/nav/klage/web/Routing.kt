package no.nav.klage.web

import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.klage.domain.BehandlingerActiveResponseView
import no.nav.klage.domain.BehandlingerFinishedResponseView
import no.nav.klage.domain.TRBehandlingerActiveResponseView
import no.nav.klage.domain.TRBehandlingerFinishedResponseView
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Type.BEGJAERING_OM_GJENOPPTAK
import no.nav.klage.kodeverk.Type.BEGJAERING_OM_GJENOPPTAK_I_TRYGDERETTEN
import no.nav.klage.kodeverk.Type.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET
import no.nav.klage.kodeverk.Type.KLAGE
import no.nav.klage.kodeverk.Type.OMGJOERINGSKRAV
import no.nav.klage.repository.BehandlingRepository
import no.nav.klage.service.KafkaClient
import no.nav.klage.service.getBehandlingListFerdigstilte
import no.nav.klage.service.getBehandlingListLedige
import no.nav.klage.service.getBehandlingListTildelte
import no.nav.klage.service.getTRBehandlingListFerdigstilte
import no.nav.klage.service.getTRBehandlingListLedige
import no.nav.klage.service.getTRBehandlingListTildelte
import no.nav.klage.util.validateToken

fun Application.configureRouting() {
    // TODO: Routes for new anke types
    routing {
        route("/api.json") {
            openApi()
        }

        route("/swagger-ui") {
            swaggerUI("/api.json")
        }

        get(path = "/klager/ledige", builder = {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListLedige(KLAGE))
        }

        get(path = "/anker/ledige", builder = {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListLedige(Type.ANKE_FOER_2027))
        }

        get(path = "/behandlinger-etter-tr-opphevet/ledige", builder = {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListLedige(BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET))
        }

        get(path = "/omgjoeringskrav/ledige", builder = {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListLedige(OMGJOERINGSKRAV))
        }

        get(path = "/anker-i-tr/ledige", builder = {
            response {
                HttpStatusCode.OK to {
                    body<TRBehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getTRBehandlingListLedige(type = Type.ANKE_I_TRYGDERETTEN_FOER_2027))
        }

        get(path = "/begjaeringer-om-gjenopptak/ledige", builder = {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListLedige(type = BEGJAERING_OM_GJENOPPTAK))
        }

        get(path = "/begjaeringer-om-gjenopptak-i-tr/ledige", builder = {
            response {
                HttpStatusCode.OK to {
                    body<TRBehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getTRBehandlingListLedige(type = BEGJAERING_OM_GJENOPPTAK_I_TRYGDERETTEN))
        }

        get(path = "/klager/tildelte", builder = {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListTildelte(KLAGE))
        }

        get(path = "/anker/tildelte", builder = {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListTildelte(Type.ANKE_FOER_2027))
        }

        get(path = "/behandlinger-etter-tr-opphevet/tildelte", builder = {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListTildelte(BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET))
        }

        get(path = "/omgjoeringskrav/tildelte", builder = {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListTildelte(OMGJOERINGSKRAV))
        }

        get(path = "/anker-i-tr/tildelte", builder = {
            response {
                HttpStatusCode.OK to {
                    body<TRBehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getTRBehandlingListTildelte(type = Type.ANKE_I_TRYGDERETTEN_FOER_2027))
        }

        get(path = "/begjaeringer-om-gjenopptak/tildelte", builder = {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListTildelte(type = BEGJAERING_OM_GJENOPPTAK))
        }

        get(path = "/begjaeringer-om-gjenopptak-i-tr/tildelte", builder = {
            response {
                HttpStatusCode.OK to {
                    body<TRBehandlingerActiveResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getTRBehandlingListTildelte(type = BEGJAERING_OM_GJENOPPTAK_I_TRYGDERETTEN))
        }

        get(path = "/klager/ferdigstilte", builder = {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerFinishedResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListFerdigstilte(KLAGE))
        }

        get(path = "/anker/ferdigstilte", builder = {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerFinishedResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListFerdigstilte(Type.ANKE_FOER_2027))
        }

        get(path = "/behandlinger-etter-tr-opphevet/ferdigstilte", builder = {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerFinishedResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListFerdigstilte(BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET))
        }

        get(path = "/omgjoeringskrav/ferdigstilte", builder = {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerFinishedResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListFerdigstilte(OMGJOERINGSKRAV))
        }

        get(path = "/anker-i-tr/ferdigstilte", builder = {
            response {
                HttpStatusCode.OK to {
                    body<TRBehandlingerFinishedResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getTRBehandlingListFerdigstilte(type = Type.ANKE_I_TRYGDERETTEN_FOER_2027))
        }

        get(path = "/begjaeringer-om-gjenopptak/ferdigstilte", builder = {
            response {
                HttpStatusCode.OK to {
                    body<BehandlingerFinishedResponseView>()
                }
            }
        }) {
            call.validateToken()
            call.respond(getBehandlingListFerdigstilte(type = BEGJAERING_OM_GJENOPPTAK))
        }

        get(path = "/begjaeringer-om-gjenopptak-i-tr/ferdigstilte", builder = {
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
                call.respond(status = HttpStatusCode.ServiceUnavailable, message = "Kafka consumer unhealthy")
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
                ),
            )
        }
    }
}
