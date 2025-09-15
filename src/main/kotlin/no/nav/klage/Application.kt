package no.nav.klage

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.github.smiley4.ktoropenapi.OpenApi
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.launch
import no.nav.klage.domain.Behandling
import no.nav.klage.repository.BehandlingRepository
import no.nav.klage.service.KabalApiService
import no.nav.klage.service.KafkaClient
import no.nav.klage.service.MockKafkaClient
import no.nav.klage.web.configureRouting
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

private val logger = LoggerFactory.getLogger("main")

val Application.envKind get() = environment.config.property("ktor.development").getString()
val Application.isDevelopmentMode get() = envKind == "true"
val Application.isProductionMode get() = envKind == "false"

suspend fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        }
    }
    install(Compression) {
        gzip()
    }
    install(OpenApi)

    if (isProductionMode) {
        val fetchJob = launch {
            KabalApiService.fetchAndStoreBehandlinger()
        }

        fetchJob.join()

        launch {
            KafkaClient.startKafkaListener()
        }
    } else if (isDevelopmentMode) {
        addMockBehandlinger()
        launch {
            MockKafkaClient.consumeMessages()
        }
    }

    logger.debug("Application is running in ${if (isDevelopmentMode) "development/local" else "production"} mode")

//    configureSockets()
    configureRouting()
}

private fun addMockBehandlinger() {
//     Add some mock data
    for (i in 1..100) {
        BehandlingRepository.addBehandling(
            Behandling(
                id = UUID.randomUUID(),
                fraNAVEnhet = "4400",
                mottattVedtaksinstans = LocalDate.now(),
                temaId = "1",
                ytelseId = "5",
                typeId = "1",
                mottattKlageinstans = LocalDate.now(),
                avsluttetAvSaksbehandlerDate = LocalDate.now(),
                isAvsluttetAvSaksbehandler = listOf(true, false).random(),
                isTildelt = listOf(true, false).random(),
                tildeltEnhet = "4295",
                frist = LocalDate.now().plusWeeks(4),
                ageKA = (1..20).random(),
                datoSendtMedunderskriver = null,
                hjemmelIdList = listOf("1", "2"),
                modified = LocalDateTime.now(),
                created = LocalDateTime.now(),
                resultat = Behandling.VedtakView(
                    id = UUID.randomUUID(),
                    utfallId = listOf("1", "2", "3").random(),
                    hjemmelIdSet = setOf("FTRL_22_12", "FTRL_22_13"),
                ),
                sattPaaVent = null,
                sendtTilTrygderetten = null,
                kjennelseMottatt = null,
                feilregistrering = null,
                fagsystemId = "123456789",
                varsletFrist = null,
                tilbakekreving = false,
            )
        )
    }
}
