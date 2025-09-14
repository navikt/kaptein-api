package no.nav.klage.service

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nav.klage.domain.Behandling
import no.nav.klage.repository.BehandlingRepository
import org.slf4j.LoggerFactory

object GraphCreator {

    private val logger = LoggerFactory.getLogger(GraphCreator::class.java.name)

    suspend fun calculateGraphs() {
        coroutineScope {
            logger.debug("Starting graph calculations")

            var filteredBehandlingSet = setOf<Behandling>()

            val mainFilterJob = launch {
                logger.debug("Starting main filter job")
                delay(1000)
                filteredBehandlingSet = BehandlingRepository.getBehandlingListCopyForReadOnly().take(1).toSet()
                logger.debug("Main filter job done")
            }

            mainFilterJob.join() // wait for the main filter to complete before starting graphs

            launch {
                graph1(filteredBehandlingSet)
            }
            launch {
                graph2(filteredBehandlingSet)
            }
            launch {
                graph3(filteredBehandlingSet)
            }
        }
        logger.debug("All graphs done")
    }

    suspend fun graph1(behandlingSet: Set<Behandling>) {
        logger.debug("Starting graph 1")
        delay(3000)
        logger.debug("Graph 1 done")
    }

    suspend fun graph2(behandlingSet: Set<Behandling>) {
        logger.debug("Starting graph 2")
        delay(1500)
        logger.debug("Graph 2 done")
    }

    suspend fun graph3(behandlingSet: Set<Behandling>) {
        logger.debug("Starting graph 3")
        delay(2000)
        logger.debug("Graph 3 done")
    }

}