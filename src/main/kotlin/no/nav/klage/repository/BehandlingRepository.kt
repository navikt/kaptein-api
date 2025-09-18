package no.nav.klage.repository

import io.ktor.util.logging.*
import no.nav.klage.domain.Behandling
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

object BehandlingRepository {

    private val logger = KtorSimpleLogger(BehandlingRepository::class.java.name)

    private val lock = ReentrantReadWriteLock()
    private val behandlingSet = HashMap<UUID, Behandling>()

    private var isReady = false

    fun getBehandlingListCopyForReadOnly(): List<Behandling> {
        // read lock allows many concurrent readers
        return lock.read {
            logger.debug("Retrieving list of behandlinger, count: {}", behandlingSet.size)
            // create a list with copied elements
            behandlingSet.values.map { it.copy() }
        }
    }

    fun addBehandling(incomingBehandling: Behandling) {
        // write lock ensures exclusive access for mutations
        lock.write {
            val behandlingInStore = behandlingSet[incomingBehandling.id]
            if (
                (behandlingInStore != null && behandlingInStore.modified < incomingBehandling.modified) ||
                behandlingInStore == null
            ) {
                behandlingSet[incomingBehandling.id] = incomingBehandling
            }
        }
    }

    fun setReady() {
        isReady = true
    }

    fun isReady(): Boolean = isReady
}