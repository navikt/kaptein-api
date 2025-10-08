package no.nav.klage.service

import io.ktor.util.logging.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import no.nav.klage.domain.Behandling
import no.nav.klage.repository.BehandlingRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

object MockKafkaClient {

    private val logger = KtorSimpleLogger(MockKafkaClient::class.java.name)

    suspend fun consumeMessages() {
        logger.debug("Mock consuming messages from Kafka")

        //every 10 seconds, add a new behandling to the repository
        coroutineScope {
            while (true) {
                val newBehandling = Behandling(
                    id = UUID.randomUUID(),
                    fraNAVEnhet = "4400",
                    mottattVedtaksinstans = LocalDate.now(),
                    temaId = "1",
                    ytelseId = "5",
                    typeId = "1",
                    mottattKlageinstans = LocalDate.now(),
                    avsluttetAvSaksbehandlerDate = LocalDate.now(),
                    isAvsluttetAvSaksbehandler = false,
                    isTildelt = false,
                    tildeltEnhet = "4295",
                    frist = LocalDate.now().plusWeeks(4),
                    ageKA = 10,
                    datoSendtMedunderskriver = null,
                    hjemmelIdList = listOf("1", "2"),
                    modified = LocalDateTime.now(),
                    created = LocalDateTime.now(),
                    resultat = null,
                    sattPaaVent = null,
                    sendtTilTrygderetten = null,
                    kjennelseMottatt = null,
                    feilregistrering = null,
                    fagsystemId = "123456789",
                    varsletFrist = null,
                    tilbakekreving = false,
                    previousTildeltEnhet = null,
                    previousRegistreringshjemmelIdList = null,
                )
                BehandlingRepository.addBehandling(newBehandling)
                logger.debug("Received new behandling from \"Kafka\": {}", newBehandling)
                delay(10_000)
            }
        }
    }
}