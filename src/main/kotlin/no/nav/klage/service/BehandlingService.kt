package no.nav.klage.service

import io.ktor.util.logging.*
import no.nav.klage.domain.*
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Type.ANKE_I_TRYGDERETTEN
import no.nav.klage.repository.BehandlingRepository

private val logger = KtorSimpleLogger("no.nav.klage.service.BehandlingService")

fun getBehandlingListLedige(type: Type): BehandlingerActiveResponseView {
    logger.debug("getBehandlingListLedige")
    val start = System.currentTimeMillis()
    val behandlingList = BehandlingRepository.getBehandlingListCopyForReadOnly()

    val behandlingViewList = behandlingList.filter { behandling ->
        behandling.feilregistrering == null &&
                !behandling.isAvsluttetAvSaksbehandler &&
                !behandling.isTildelt &&
                behandling.typeId == type.id
    }.map { it.toActiveView() }

    logger.debug("Fetched ${behandlingViewList.size} ledige behandlinger with type $type in ${System.currentTimeMillis() - start} ms")

    return BehandlingerActiveResponseView(
        behandlinger = behandlingViewList,
        total = behandlingViewList.size,
    )
}

fun getTRBehandlingListLedige(): TRBehandlingerActiveResponseView {
    logger.debug("getTRBehandlingListLedige")
    val start = System.currentTimeMillis()
    val behandlingList = BehandlingRepository.getBehandlingListCopyForReadOnly()

    val behandlingViewList = behandlingList.filter { behandling ->
        behandling.feilregistrering == null &&
                !behandling.isAvsluttetAvSaksbehandler &&
                !behandling.isTildelt &&
                behandling.typeId == ANKE_I_TRYGDERETTEN.id
    }.map { it.toTRActiveView() }

    logger.debug("Fetched ${behandlingViewList.size} ledige behandlinger with type ${ANKE_I_TRYGDERETTEN.id} in ${System.currentTimeMillis() - start} ms")

    return TRBehandlingerActiveResponseView(
        behandlinger = behandlingViewList,
        total = behandlingViewList.size,
    )
}

fun getBehandlingListTildelte(type: Type): BehandlingerActiveResponseView {
    logger.debug("getBehandlingListTildelte")
    val start = System.currentTimeMillis()
    val behandlingList = BehandlingRepository.getBehandlingListCopyForReadOnly()

    val behandlingViewList = behandlingList.filter { behandling ->
        behandling.feilregistrering == null &&
                !behandling.isAvsluttetAvSaksbehandler &&
                behandling.isTildelt &&
                behandling.typeId == type.id
    }.map { it.toActiveView() }

    logger.debug("Fetched ${behandlingViewList.size} tildelte behandlinger with type $type in ${System.currentTimeMillis() - start} ms")

    return BehandlingerActiveResponseView(
        behandlinger = behandlingViewList,
        total = behandlingViewList.size,
    )
}

fun getTRBehandlingListTildelte(): TRBehandlingerActiveResponseView {
    logger.debug("getTRBehandlingListTildelte")
    val start = System.currentTimeMillis()
    val behandlingList = BehandlingRepository.getBehandlingListCopyForReadOnly()

    val behandlingViewList = behandlingList.filter { behandling ->
        behandling.feilregistrering == null &&
                !behandling.isAvsluttetAvSaksbehandler &&
                behandling.isTildelt &&
                behandling.typeId == ANKE_I_TRYGDERETTEN.id
    }.map { it.toTRActiveView() }

    logger.debug("Fetched ${behandlingViewList.size} tildelte behandlinger with type ${ANKE_I_TRYGDERETTEN.id} in ${System.currentTimeMillis() - start} ms")

    return TRBehandlingerActiveResponseView(
        behandlinger = behandlingViewList,
        total = behandlingViewList.size,
    )
}

fun getBehandlingListFerdigstilte(type: Type): BehandlingerFinishedResponseView {
    logger.debug("getBehandlingListFerdigstilte")
    val start = System.currentTimeMillis()
    val behandlingList = BehandlingRepository.getBehandlingListCopyForReadOnly()

    val behandlingViewList = behandlingList.filter { behandling ->
        behandling.feilregistrering == null &&
                behandling.isAvsluttetAvSaksbehandler &&
                behandling.typeId == type.id
    }.map {
        BehandlingFinishedView(
            id = it.id.toString(),
            ytelseId = it.ytelseId,
            typeId = it.typeId,
            avsluttetAvSaksbehandlerDate = it.avsluttetAvSaksbehandlerDate!!,
            tildeltEnhet = it.tildeltEnhet!!,
            frist = it.frist,
            ageKA = it.ageKA,
            innsendingshjemmelIdList = it.hjemmelIdList,
            created = it.created,
            mottattKlageinstans = it.mottattKlageinstans,
            resultat = VedtakView(
                utfallId = it.resultat?.utfallId!!,
                registreringshjemmelIdList = it.resultat.hjemmelIdSet.toList(),
            ),
            varsletFrist = it.varsletFrist,
            tilbakekreving = it.tilbakekreving
        )

    }
    logger.debug("Fetched ${behandlingViewList.size} finished behandlinger with type $type in ${System.currentTimeMillis() - start} ms")
    return BehandlingerFinishedResponseView(
        behandlinger = behandlingViewList,
        total = behandlingViewList.size,
    )
}

fun getTRBehandlingListFerdigstilte(): TRBehandlingerFinishedResponseView {
    logger.debug("getTRBehandlingListFerdigstilte")
    val start = System.currentTimeMillis()
    val behandlingList = BehandlingRepository.getBehandlingListCopyForReadOnly()

    val behandlingViewList = behandlingList.filter { behandling ->
        behandling.feilregistrering == null &&
                behandling.isAvsluttetAvSaksbehandler &&
                behandling.typeId == ANKE_I_TRYGDERETTEN.id
    }.map {
        TRBehandlingFinishedView(
            id = it.id.toString(),
            ytelseId = it.ytelseId,
            typeId = it.typeId,
            avsluttetAvSaksbehandlerDate = it.avsluttetAvSaksbehandlerDate!!,
            tildeltEnhet = it.previousTildeltEnhet ?: it.tildeltEnhet!!,
            ageKA = it.ageKA,
            innsendingshjemmelIdList = it.hjemmelIdList,
            created = it.created,
            mottattKlageinstans = it.mottattKlageinstans,
            resultat = VedtakView(
                utfallId = it.resultat?.utfallId!!,
                registreringshjemmelIdList = it.resultat.hjemmelIdSet.toList(),
            ),
            tilbakekreving = it.tilbakekreving,
            previousRegistreringshjemmelIdList = it.previousRegistreringshjemmelIdList,
            sendtTilTR = it.sendtTilTrygderetten!!.toLocalDate(),
            mottattFraTR = it.kjennelseMottatt!!.toLocalDate(),
        )
    }
    logger.debug("Fetched ${behandlingViewList.size} finished behandlinger with type ${ANKE_I_TRYGDERETTEN.id} in ${System.currentTimeMillis() - start} ms")
    return TRBehandlingerFinishedResponseView(
        behandlinger = behandlingViewList,
        total = behandlingViewList.size,
    )
}

fun Behandling.toActiveView(): BehandlingActiveView {
    return BehandlingActiveView(
        id = this.id.toString(),
        ytelseId = this.ytelseId,
        typeId = this.typeId,
        isTildelt = this.isTildelt,
        tildeltEnhet = this.tildeltEnhet,
        frist = this.frist,
        ageKA = this.ageKA,
        innsendingshjemmelIdList = this.hjemmelIdList,
        created = this.created,
        mottattKlageinstans = this.mottattKlageinstans,
        sattPaaVentReasonId = this.sattPaaVent?.reasonId,
        varsletFrist = this.varsletFrist,
        tilbakekreving = this.hjemmelIdList.isTilbakekreving()
    )
}

fun Behandling.toTRActiveView(): TRBehandlingActiveView {
    return TRBehandlingActiveView(
        id = this.id.toString(),
        ytelseId = this.ytelseId,
        typeId = this.typeId,
        isTildelt = this.isTildelt,
        tildeltEnhet = this.previousTildeltEnhet ?: this.tildeltEnhet,
        ageKA = this.ageKA,
        innsendingshjemmelIdList = this.hjemmelIdList,
        created = this.created,
        mottattKlageinstans = this.mottattKlageinstans,
        tilbakekreving = this.hjemmelIdList.isTilbakekreving(),
        previousRegistreringshjemmelIdList = this.previousRegistreringshjemmelIdList,
        sendtTilTR = this.sendtTilTrygderetten!!.toLocalDate(),
    )
}

private fun List<String>.isTilbakekreving(): Boolean {
    val tilbakekrevinghjemler = setOf(
        "FTRL_22_15_TILBAKEKREVING",
        "FTRL_22_15_TILBAKEKREVING_DOEDSBO",
        "1000.022.015",
        "FTRL_22_15_1_1",
        "FTRL_22_15_1_2",
        "FTRL_22_15_2",
        "FTRL_22_15_4",
        "FTRL_22_15_5",
        "FTRL_22_15_6",
        "FTRL_22_17A",
        "FTRL_4_28",
        "596",
        "614",
        "706",
    )
    return this.any { it in tilbakekrevinghjemler }
}