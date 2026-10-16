package com.jnasser.pokedex.feature.pokemonlist

import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PokemonListViewModel(
    getPagedPokemon: GetPagedPokemonUseCase,
    observeCatalogStatus: ObserveCatalogStatusUseCase,
    private val setFavorite: SetFavoriteUseCase,
    private val synchronizeCatalog: SynchronizePokemonCatalogUseCase,
) : ViewModel() {
    val pokemon = getPagedPokemon(query = "").cachedIn(viewModelScope)

    val uiState = observeCatalogStatus()
        .map(CatalogStatus::toPokemonListUiState)
        .catch {
            emit(
                PokemonListUiState(
                    isInitialLoading = false,
                    initialError = SyncFailure.STORAGE,
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = PokemonListUiState(),
        )

    private val effectChannel = Channel<PokemonListEffect>(capacity = Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var synchronizationJob: Job? = null

    fun onAction(action: PokemonListAction) {
        when (action) {
            PokemonListAction.RetrySynchronization -> retrySynchronization()
            is PokemonListAction.FavoriteChanged -> updateFavorite(action)
        }
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

private fun CatalogStatus.toPokemonListUiState(): PokemonListUiState {
    val failure = (syncState as? CatalogSyncState.Failed)?.reason

    return PokemonListUiState(
        storedPokemonCount = storedPokemonCount,
        hasContent = hasUsableData,
        isInitialLoading = !hasUsableData && syncState !is CatalogSyncState.Failed,
        initialError = failure.takeIf { !hasUsableData },
        isRefreshing = hasUsableData && syncState is CatalogSyncState.Syncing,
        refreshError = failure.takeIf { hasUsableData },
    )
}
