package com.jnasser.pokedex.sync

import com.jnasser.pokedex.core.domain.usecase.SynchronizePokemonCatalogUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CatalogSyncCoordinator(
    private val synchronizeCatalog: SynchronizePokemonCatalogUseCase,
    private val applicationScope: CoroutineScope,
) {
    private val lock = Any()
    private var activeJob: Job? = null

    fun synchronize(): Job = synchronized(lock) {
        activeJob?.takeUnless(Job::isCompleted) ?: createSyncJob()
    }

    private fun createSyncJob(): Job {
        val job = applicationScope.launch(start = CoroutineStart.LAZY) {
            synchronizeCatalog()
        }
        activeJob = job
        job.invokeOnCompletion {
            synchronized(lock) {
                if (activeJob === job) activeJob = null
            }
        }
        job.start()
        return job
    }
}
