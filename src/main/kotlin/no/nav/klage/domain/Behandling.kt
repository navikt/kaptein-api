package no.nav.klage.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class Behandling(
    val id: UUID,
    val fraNAVEnhet: String?,
    val mottattVedtaksinstans: LocalDate?,
    val temaId: String,
    val ytelseId: String,
    val typeId: String,
    val mottattKlageinstans: LocalDate,
    val avsluttetAvSaksbehandlerDate: LocalDate?,
    val isAvsluttetAvSaksbehandler: Boolean,
    val isTildelt: Boolean,
    val tildeltEnhet: String?,
    val frist: LocalDate?,
    val ageKA: Int,
    val datoSendtMedunderskriver: LocalDate?,
    val hjemmelIdList: List<String>,
    val modified: LocalDateTime,
    val created: LocalDateTime,
    val resultat: VedtakView?,
    val sattPaaVent: SattPaaVent?,
    val sendtTilTrygderetten: LocalDateTime?,
    val kjennelseMottatt: LocalDateTime?,
    val feilregistrering: FeilregistreringView?,
    val fagsystemId: String,
    val varsletFrist: LocalDate?,
    val tilbakekreving: Boolean,
    val previousTildeltEnhet: String?,
    val previousRegistreringshjemmelIdList: List<String>?,
) {
    data class VedtakView(
        val id: UUID,
        val utfallId: String?,
        val hjemmelIdSet: Set<String>,
    )

    data class FeilregistreringView(
        val registered: LocalDateTime,
        val reason: String,
        val fagsystemId: String,
    )

    data class SattPaaVent(
        val from: LocalDate,
        val to: LocalDate,
        val reason: String?,
        val reasonId: String,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Behandling) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}