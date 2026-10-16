package com.jnasser.pokedex.feature.pokemonlist

import androidx.paging.PagingData
import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
    fun `initial query requests the complete local catalog after debounce`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.pokemon.collect { }
        }

        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS)
        runCurrent()

        assertEquals(listOf(""), repository.pagingQueries)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `query changes are debounced normalized and use only the latest value`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.pokemon.collect { }
        }
        viewModel.onAction(PokemonListAction.SearchQueryChanged("Pika"))
        advanceTimeBy(200)

        viewModel.onAction(PokemonListAction.SearchQueryChanged("  Pikachu  "))
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS - 1)
        runCurrent()

        assertTrue(repository.pagingQueries.isEmpty())

        advanceTimeBy(1)
        runCurrent()

        assertEquals(listOf("pikachu"), repository.pagingQueries)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `equivalent normalized queries do not restart paging`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.pokemon.collect { }
        }
        viewModel.onAction(PokemonListAction.SearchQueryChanged("  #0025  "))
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS)
        runCurrent()

        viewModel.onAction(PokemonListAction.SearchQueryChanged("#0025"))
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS)
        runCurrent()

        assertEquals(listOf("#0025"), repository.pagingQueries)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `saved query is restored in state and used by paging`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(SEARCH_QUERY_KEY to "25"))
        val viewModel = createViewModel(savedStateHandle)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.pokemon.collect { }
        }

        assertEquals("25", viewModel.uiState.value.searchQuery)

        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS)
        runCurrent()

        assertEquals(listOf("25"), repository.pagingQueries)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `clear search removes the persisted query`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(SEARCH_QUERY_KEY to "charizard"))
        val viewModel = createViewModel(savedStateHandle)

        viewModel.onAction(PokemonListAction.ClearSearch)
        runCurrent()

        assertEquals("", savedStateHandle[SEARCH_QUERY_KEY])
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

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = PokemonListViewModel(
        savedStateHandle = savedStateHandle,
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
