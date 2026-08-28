package no.nav.klage.repository

import io.ktor.util.logging.KtorSimpleLogger
import no.nav.klage.domain.Behandling
import java.util.UUID
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

    /**
     * Diagnostics helper: a cheap, order-independent fingerprint of the current state.
     * Two instances that hold identical data (same ids at the same 'modified' versions)
     * will produce the same count and checksum. Compare across pods to detect drift.
     */
    fun getStateFingerprint(): StateFingerprint =
        lock.read {
            // order-independent: XOR per-entry hashes so insertion order does not matter
            var checksum = 0L
            var newestModified: java.time.LocalDateTime? = null
            for (b in behandlingSet.values) {
                checksum = checksum xor (b.id.hashCode().toLong() * 31 + b.modified.hashCode())
                if (newestModified == null || b.modified.isAfter(newestModified)) {
                    newestModified = b.modified
                }
            }
            StateFingerprint(
                count = behandlingSet.size,
                checksum = checksum,
                newestModified = newestModified?.toString(),
            )
        }

    data class StateFingerprint(
        val count: Int,
        val checksum: Long,
        val newestModified: String?,
    )

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
