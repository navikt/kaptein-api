package no.nav.klage.repository

import no.nav.klage.domain.Behandling
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

object BehandlingRepository {

    private val logger = LoggerFactory.getLogger(BehandlingRepository::class.java.name)

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

    fun addBehandling(behandling: Behandling) {
        // write lock ensures exclusive access for mutations
        lock.write {
            if (behandlingSet.contains(behandling)) {
                logger.debug("Behandling already exists in store, replacing: {}", behandling.id)
                behandlingSet.remove(behandling)
                behandlingSet.add(behandling)
            } else {
                behandlingSet.add(behandling)
                logger.debug("Added behandling to store. Size is now: {}", behandlingSet.size)
            }
        }
    }

    fun clearAndAddAll(behandlinger: Collection<Behandling>) {
        lock.write {
            behandlingSet.clear()
            behandlingSet.addAll(behandlinger)
            logger.debug("Added ${behandlinger.size} behandlinger to store. Size is now: {}", behandlingSet.size)
        }
        isReady = true
    }

    fun isReady(): Boolean = isReady
}