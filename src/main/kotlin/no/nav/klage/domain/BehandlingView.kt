package no.nav.klage.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class BehandlingerFinishedResponseView(
    val behandlinger: List<BehandlingFinishedView>,
    val total: Int,
)

data class BehandlingerActiveResponseView(
    val behandlinger: List<BehandlingActiveView>,
    val total: Int,
)

data class BehandlingFinishedView(
    val id: String,
    val ytelseId: String,
    val typeId: String,
    val avsluttetAvSaksbehandlerDate: LocalDate,
    val tildeltEnhet: String,
    val frist: LocalDate?,
    val ageKA: Int,
    val hjemmelIdList: List<String>,
    val created: LocalDateTime,
    val resultat: VedtakView,
    val varsletFrist: LocalDate?,
    val tilbakekreving: Boolean,
) {
    data class VedtakView(
        val utfallId: String,
        val hjemmelIdSet: Set<String>,
    )
}

data class BehandlingActiveView(
    val id: String,
    val ytelseId: String,
    val typeId: String,
    val isTildelt: Boolean,
    val tildeltEnhet: String?,
    val frist: LocalDate?,
    val ageKA: Int,
    val hjemmelIdList: List<String>,
    val created: LocalDateTime,
    val sattPaaVent: SattPaaVent?,
    val varsletFrist: LocalDate?,
    val tilbakekreving: Boolean,
) {
    data class SattPaaVent(
        val reasonId: String,
    )
}