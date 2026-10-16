package com.jnasser.pokedex.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jnasser.pokedex.core.domain.usecase.ObserveFavoritesUseCase
import com.jnasser.pokedex.core.domain.usecase.SetFavoriteUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val observeFavorites: ObserveFavoritesUseCase,
    private val setFavorite: SetFavoriteUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState = mutableUiState.asStateFlow()

    private val effectChannel = Channel<FavoritesEffect>(capacity = Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var observationJob: Job? = null

    init {
        observeFavoritePokemon()
    }

    fun onAction(action: FavoritesAction) {
        when (action) {
            FavoritesAction.Retry -> observeFavoritePokemon()
            is FavoritesAction.RemoveFavorite -> removeFavorite(action.pokemonId)
        }
    }

    private fun observeFavoritePokemon() {
        observationJob?.cancel()
        mutableUiState.value = FavoritesUiState.Loading
        observationJob = viewModelScope.launch {
            try {
                observeFavorites().collect { favorites ->
                    mutableUiState.value = if (favorites.isEmpty()) {
                        FavoritesUiState.Empty
                    } else {
                        FavoritesUiState.Content(favorites)
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                mutableUiState.value = FavoritesUiState.Error
            }
        }
    }

    private fun removeFavorite(pokemonId: Int) {
        viewModelScope.launch {
            try {
                setFavorite(pokemonId, false)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                effectChannel.send(FavoritesEffect.FavoriteUpdateFailed)
            }
        }
    }
}
