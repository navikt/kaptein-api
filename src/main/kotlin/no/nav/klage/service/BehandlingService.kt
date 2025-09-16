package no.nav.klage.service

import io.ktor.util.logging.*
import no.nav.klage.domain.*
import no.nav.klage.repository.BehandlingRepository

private val logger = KtorSimpleLogger("no.nav.klage.service.BehandlingService")

suspend fun getBehandlingListLedige(): BehandlingerActiveResponseView {
    logger.debug("getBehandlingListLedige")
    val start = System.currentTimeMillis()
    val behandlingList = BehandlingRepository.getBehandlingListCopyForReadOnly()

    val behandlingViewList = behandlingList.filter { behandling ->
        behandling.typeId != "3" && //anke i TR
                behandling.feilregistrering == null &&
                !behandling.isAvsluttetAvSaksbehandler &&
                !behandling.isTildelt
    }.map { it.toActiveView() }

    logger.debug("Fetched ${behandlingViewList.size} ledige behandlinger in ${System.currentTimeMillis() - start} ms")

    return BehandlingerActiveResponseView(
        behandlinger = behandlingViewList,
        total = behandlingViewList.size,
    )
}

suspend fun getBehandlingListTildelte(): BehandlingerActiveResponseView {
    logger.debug("getBehandlingListTildelte")
    val start = System.currentTimeMillis()
    val behandlingList = BehandlingRepository.getBehandlingListCopyForReadOnly()

    val behandlingViewList = behandlingList.filter { behandling ->
        behandling.typeId != "3" && //anke i TR
                behandling.feilregistrering == null &&
                !behandling.isAvsluttetAvSaksbehandler &&
                behandling.isTildelt
    }.map { it.toActiveView() }

    logger.debug("Fetched ${behandlingViewList.size} tildelte behandlinger in ${System.currentTimeMillis() - start} ms")

    return BehandlingerActiveResponseView(
        behandlinger = behandlingViewList,
        total = behandlingViewList.size,
    )
}

suspend fun getBehandlingListFerdigstilte(): BehandlingerFinishedResponseView {
    logger.debug("getBehandlingListFerdigstilte")
    val start = System.currentTimeMillis()
    val behandlingList = BehandlingRepository.getBehandlingListCopyForReadOnly()

    val behandlingViewList = behandlingList.filter { behandling ->
        behandling.typeId != "3" && //anke i TR
                behandling.feilregistrering == null &&
                behandling.isAvsluttetAvSaksbehandler
    }.map {
        BehandlingFinishedView(
            id = it.id,
            ytelseId = it.ytelseId,
            typeId = it.typeId,
            avsluttetAvSaksbehandlerDate = it.avsluttetAvSaksbehandlerDate,
            tildeltEnhet = it.tildeltEnhet,
            frist = it.frist,
            ageKA = it.ageKA,
            hjemmelIdList = it.hjemmelIdList,
            created = it.created,
            resultat = BehandlingFinishedView.VedtakView(
                utfallId = it.resultat?.utfallId!!,
                hjemmelIdSet = it.resultat.hjemmelIdSet,
            ),
            varsletFrist = it.varsletFrist,
            tilbakekreving = it.tilbakekreving
        )

    }
    logger.debug("Fetched ${behandlingViewList.size} finished behandlinger in ${System.currentTimeMillis() - start} ms")
    return BehandlingerFinishedResponseView(
        behandlinger = behandlingViewList,
        total = behandlingViewList.size,
    )
}

fun Behandling.toActiveView(): BehandlingActiveView {
    return BehandlingActiveView(
        id = this.id,
        ytelseId = this.ytelseId,
        typeId = this.typeId,
        isTildelt = this.isTildelt,
        tildeltEnhet = this.tildeltEnhet,
        frist = this.frist,
        ageKA = this.ageKA,
        hjemmelIdList = this.hjemmelIdList,
        created = this.created,
        sattPaaVent = if (this.sattPaaVent != null) BehandlingActiveView.SattPaaVent(
            reasonId = this.sattPaaVent.reasonId
        ) else null,
        varsletFrist = this.varsletFrist,
        tilbakekreving = this.tilbakekreving
    )
}