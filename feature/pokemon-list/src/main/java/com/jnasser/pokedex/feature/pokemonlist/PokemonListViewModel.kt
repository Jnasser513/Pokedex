package com.jnasser.pokedex.feature.pokemonlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.jnasser.pokedex.core.domain.usecase.GetPagedPokemonUseCase
import com.jnasser.pokedex.core.domain.usecase.ObserveCatalogStatusUseCase
import com.jnasser.pokedex.core.domain.usecase.SetFavoriteUseCase
import com.jnasser.pokedex.core.domain.usecase.SynchronizePokemonCatalogUseCase
import com.jnasser.pokedex.core.model.CatalogStatus
import com.jnasser.pokedex.core.model.CatalogSyncState
import com.jnasser.pokedex.core.model.SyncFailure
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PokemonListViewModel(
    private val savedStateHandle: SavedStateHandle,
    getPagedPokemon: GetPagedPokemonUseCase,
    observeCatalogStatus: ObserveCatalogStatusUseCase,
    private val setFavorite: SetFavoriteUseCase,
    private val synchronizeCatalog: SynchronizePokemonCatalogUseCase,
) : ViewModel() {
    private val searchQuery = savedStateHandle.getStateFlow(SEARCH_QUERY_KEY, "")

    val pokemon = searchQuery
        .debounce(SEARCH_DEBOUNCE_MILLIS)
        .map(String::trim)
        .distinctUntilChanged()
        .flatMapLatest(getPagedPokemon::invoke)
        .cachedIn(viewModelScope)

    val uiState = combine(
        observeCatalogStatus(),
        searchQuery,
    ) { catalogStatus, query ->
        catalogStatus.toPokemonListUiState(searchQuery = query)
    }
        .catch {
            emit(
                PokemonListUiState(
                    searchQuery = searchQuery.value,
                    isInitialLoading = false,
                    initialError = SyncFailure.STORAGE,
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = PokemonListUiState(searchQuery = searchQuery.value),
        )

    private val effectChannel = Channel<PokemonListEffect>(capacity = Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var synchronizationJob: Job? = null

    fun onAction(action: PokemonListAction) {
        when (action) {
            is PokemonListAction.SearchQueryChanged -> updateSearchQuery(action.query)
            PokemonListAction.ClearSearch -> updateSearchQuery("")
            PokemonListAction.RetrySynchronization -> retrySynchronization()
            is PokemonListAction.FavoriteChanged -> updateFavorite(action)
        }
    }

    private fun updateSearchQuery(query: String) {
        savedStateHandle[SEARCH_QUERY_KEY] = query
    }

    private fun retrySynchronization() {
        if (synchronizationJob?.isActive == true) return

        synchronizationJob = viewModelScope.launch {
            synchronizeCatalog()
        }
    }

    private fun updateFavorite(action: PokemonListAction.FavoriteChanged) {
        viewModelScope.launch {
            try {
                setFavorite(action.pokemonId, action.isFavorite)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                effectChannel.send(PokemonListEffect.FavoriteUpdateFailed)
            }
        }
    }
}

private fun CatalogStatus.toPokemonListUiState(searchQuery: String): PokemonListUiState {
    val failure = (syncState as? CatalogSyncState.Failed)?.reason

    return PokemonListUiState(
        searchQuery = searchQuery,
        storedPokemonCount = storedPokemonCount,
        hasContent = hasUsableData,
        isInitialLoading = !hasUsableData && syncState !is CatalogSyncState.Failed,
        initialError = failure.takeIf { !hasUsableData },
        isRefreshing = hasUsableData && syncState is CatalogSyncState.Syncing,
        refreshError = failure.takeIf { hasUsableData },
    )
}

internal const val SEARCH_QUERY_KEY = "pokemon_search_query"
internal const val SEARCH_DEBOUNCE_MILLIS = 300L
