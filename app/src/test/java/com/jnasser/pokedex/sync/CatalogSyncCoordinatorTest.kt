package com.jnasser.pokedex.sync

import androidx.paging.PagingData
import com.jnasser.pokedex.core.domain.repository.PokemonRepository
import com.jnasser.pokedex.core.domain.usecase.SynchronizePokemonCatalogUseCase
import com.jnasser.pokedex.core.model.CatalogStatus
import com.jnasser.pokedex.core.model.CatalogSyncResult
import com.jnasser.pokedex.core.model.PokemonDetail
import com.jnasser.pokedex.core.model.PokemonSummary
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class CatalogSyncCoordinatorTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `concurrent requests share one synchronization job`() = runTest {
        val repository = BlockingPokemonRepository()
        val coordinator = CatalogSyncCoordinator(
            synchronizeCatalog = SynchronizePokemonCatalogUseCase(repository),
            applicationScope = this,
        )

        val first = coordinator.synchronize()
        val second = coordinator.synchronize()
        runCurrent()

        assertSame(first, second)
        assertEquals(1, repository.syncInvocationCount)

        repository.allowCompletion.complete(Unit)
        first.join()
        coordinator.synchronize().join()

        assertEquals(2, repository.syncInvocationCount)
    }
}

private class BlockingPokemonRepository : PokemonRepository {
    val allowCompletion = CompletableDeferred<Unit>()
    var syncInvocationCount = 0

    override fun getPagedPokemon(query: String): Flow<PagingData<PokemonSummary>> =
        flowOf(PagingData.empty())

    override fun observePokemon(id: Int): Flow<PokemonDetail?> = flowOf(null)

    override fun observeFavorites(): Flow<List<PokemonSummary>> = flowOf(emptyList())

    override fun observeCatalogStatus(): Flow<CatalogStatus> = emptyFlow()

    override suspend fun setFavorite(id: Int, isFavorite: Boolean) = Unit

    override suspend fun synchronizeCatalog(): CatalogSyncResult {
        syncInvocationCount += 1
        allowCompletion.await()
        return CatalogSyncResult.Success(synchronizedPokemonCount = 151)
    }
}
