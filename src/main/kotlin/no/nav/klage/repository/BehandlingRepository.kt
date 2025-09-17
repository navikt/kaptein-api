package no.nav.klage.repository

import io.ktor.util.logging.*
import no.nav.klage.domain.Behandling
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

object BehandlingRepository {

    private val logger = KtorSimpleLogger(BehandlingRepository::class.java.name)

    private val lock = ReentrantReadWriteLock()
    private val behandlingSet = mutableSetOf<Behandling>()

    private var isReady = false

    fun getBehandlingListCopyForReadOnly(): Set<Behandling> {
        // read lock allows many concurrent readers
        return lock.read {
            logger.debug("Retrieving list of behandlinger, count: {}", behandlingSet.size)
            // create a new Set with copied elements
            behandlingSet.mapTo(mutableSetOf()) { it.copy() }
        }
    }

    fun addBehandling(incomingBehandling: Behandling) {
        // write lock ensures exclusive access for mutations
        lock.write {
            val behandlingInStore = behandlingSet.find { incomingBehandling.id == it.id }
            if (behandlingInStore != null) {
                if (behandlingInStore.modified < incomingBehandling.modified) {
                    logger.debug("Behandling in store is older than incoming, replacing.")
                    behandlingSet.remove(behandlingInStore)
                    behandlingSet.add(incomingBehandling)
                } else {
                    logger.debug("Behandling in store is newer than incoming, ignoring incoming.")
                }
            } else {
                behandlingSet.add(incomingBehandling)
            }
        }
    }

    fun setReady() {
        isReady = true
    }

    fun isReady(): Boolean = isReady
}