package com.jnasser.pokedex.feature.pokemonlist

import androidx.paging.PagingData
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.jnasser.pokedex.core.domain.repository.PokemonRepository
import com.jnasser.pokedex.core.domain.usecase.GetPagedPokemonUseCase
import com.jnasser.pokedex.core.domain.usecase.ObserveCatalogStatusUseCase
import com.jnasser.pokedex.core.domain.usecase.SetFavoriteUseCase
import com.jnasser.pokedex.core.domain.usecase.SynchronizePokemonCatalogUseCase
import com.jnasser.pokedex.core.model.CatalogStatus
import com.jnasser.pokedex.core.model.CatalogSyncResult
import com.jnasser.pokedex.core.model.CatalogSyncState
import com.jnasser.pokedex.core.model.PokemonDetail
import com.jnasser.pokedex.core.model.PokemonSummary
import com.jnasser.pokedex.core.model.SyncFailure
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakePokemonRepository()

    @Test
    fun `without local data the initial synchronization keeps a blocking loading state`() = runTest {
        repository.catalogStatus.value = catalogStatus(
            storedCount = 0,
            syncState = CatalogSyncState.Syncing,
        )
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val loadingState = awaitItem()

            assertTrue(loadingState.isInitialLoading)
            assertEquals(false, loadingState.hasContent)
        }
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `remote failure without local data becomes a blocking retryable error`() = runTest {
        repository.catalogStatus.value = catalogStatus(
            storedCount = 0,
            syncState = CatalogSyncState.Failed(SyncFailure.NETWORK),
        )
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val errorState = awaitItem()

            assertEquals(SyncFailure.NETWORK, errorState.initialError)
            assertEquals(false, errorState.isInitialLoading)
            assertEquals(false, errorState.hasContent)
        }
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `remote failure with local data preserves content and exposes a non blocking warning`() = runTest {
        repository.catalogStatus.value = catalogStatus(
            storedCount = 151,
            syncState = CatalogSyncState.Failed(SyncFailure.SERVER),
        )
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val contentState = awaitItem()

            assertTrue(contentState.hasContent)
            assertEquals(151, contentState.storedPokemonCount)
            assertEquals(SyncFailure.SERVER, contentState.refreshError)
            assertEquals(null, contentState.initialError)
        }
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `synchronization with local data keeps content visible`() = runTest {
        repository.catalogStatus.value = catalogStatus(
            storedCount = 25,
            syncState = CatalogSyncState.Syncing,
        )
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val refreshingState = awaitItem()

            assertTrue(refreshingState.hasContent)
            assertTrue(refreshingState.isRefreshing)
            assertEquals(false, refreshingState.isInitialLoading)
        }
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `initialization requests the complete local catalog`() = runTest {
        val viewModel = createViewModel()

        assertEquals(listOf(""), repository.pagingQueries)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `favorite intent delegates the requested value`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(
            PokemonListAction.FavoriteChanged(
                pokemonId = 4,
                isFavorite = true,
            )
        )
        advanceUntilIdle()

        assertEquals(4 to true, repository.lastFavoriteUpdate)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `favorite failure emits a single transient effect`() = runTest {
        repository.favoriteFailure = IllegalStateException("database unavailable")
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onAction(PokemonListAction.FavoriteChanged(4, true))

            assertEquals(PokemonListEffect.FavoriteUpdateFailed, awaitItem())
            expectNoEvents()
        }
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `repeated retry intents do not start concurrent synchronizations`() = runTest {
        repository.syncGate = CompletableDeferred()
        val viewModel = createViewModel()

        viewModel.onAction(PokemonListAction.RetrySynchronization)
        runCurrent()
        viewModel.onAction(PokemonListAction.RetrySynchronization)
        runCurrent()

        assertEquals(1, repository.syncCalls)

        repository.syncGate?.complete(Unit)
        advanceUntilIdle()
        viewModel.viewModelScope.cancel()
    }

    private fun createViewModel() = PokemonListViewModel(
        getPagedPokemon = GetPagedPokemonUseCase(repository),
        observeCatalogStatus = ObserveCatalogStatusUseCase(repository),
        setFavorite = SetFavoriteUseCase(repository),
        synchronizeCatalog = SynchronizePokemonCatalogUseCase(repository),
    )
}

private class FakePokemonRepository : PokemonRepository {
    val catalogStatus = MutableStateFlow(catalogStatus())
    var pokemon: List<PokemonSummary> = emptyList()
    val pagingQueries = mutableListOf<String>()
    var lastFavoriteUpdate: Pair<Int, Boolean>? = null
    var favoriteFailure: Throwable? = null
    var syncGate: CompletableDeferred<Unit>? = null
    var syncCalls: Int = 0

    override fun getPagedPokemon(query: String): Flow<PagingData<PokemonSummary>> {
        pagingQueries += query
        return flowOf(PagingData.from(pokemon))
    }

    override fun observePokemon(id: Int): Flow<PokemonDetail?> = flowOf(null)

    override fun observeFavorites(): Flow<List<PokemonSummary>> = flowOf(emptyList())

    override fun observeCatalogStatus(): Flow<CatalogStatus> = catalogStatus

    override suspend fun setFavorite(id: Int, isFavorite: Boolean) {
        favoriteFailure?.let { throw it }
        lastFavoriteUpdate = id to isFavorite
    }

    override suspend fun synchronizeCatalog(): CatalogSyncResult {
        syncCalls += 1
        syncGate?.await()
        return CatalogSyncResult.Success(synchronizedPokemonCount = pokemon.size)
    }
}

private fun catalogStatus(
    storedCount: Int = 0,
    syncState: CatalogSyncState = CatalogSyncState.Idle,
) = CatalogStatus(
    storedPokemonCount = storedCount,
    expectedPokemonCount = 151,
    lastSuccessfulSyncEpochMillis = null,
    syncState = syncState,
)
