package com.jnasser.pokedex.feature.pokemondetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jnasser.pokedex.core.domain.usecase.ObservePokemonDetailUseCase
import com.jnasser.pokedex.core.domain.usecase.SetFavoriteUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class PokemonDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val observePokemonDetail: ObservePokemonDetailUseCase,
    private val setFavorite: SetFavoriteUseCase,
) : ViewModel() {
    private val pokemonId: Int? = savedStateHandle[POKEMON_ID_ARGUMENT]

    private val mutableUiState = MutableStateFlow<PokemonDetailUiState>(
        PokemonDetailUiState.Loading
    )
    val uiState = mutableUiState.asStateFlow()

    private val effectChannel = Channel<PokemonDetailEffect>(capacity = Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var observationJob: Job? = null

    init {
        observePokemon()
    }

    fun onAction(action: PokemonDetailAction) {
        when (action) {
            PokemonDetailAction.Retry -> observePokemon()
            is PokemonDetailAction.FavoriteChanged -> updateFavorite(action.isFavorite)
        }
    }

    private fun observePokemon() {
        observationJob?.cancel()
        val validPokemonId = pokemonId
        if (validPokemonId == null || validPokemonId <= 0) {
            mutableUiState.value = PokemonDetailUiState.InvalidPokemonId
            return
        }

        mutableUiState.value = PokemonDetailUiState.Loading
        observationJob = viewModelScope.launch {
            try {
                observePokemonDetail(validPokemonId).collect { pokemon ->
                    mutableUiState.value = if (pokemon == null) {
                        PokemonDetailUiState.NotFound
                    } else {
                        PokemonDetailUiState.Content(pokemon)
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                mutableUiState.value = PokemonDetailUiState.Error
            }
        }
    }

    private fun updateFavorite(isFavorite: Boolean) {
        val validPokemonId = pokemonId?.takeIf { it > 0 } ?: return
        viewModelScope.launch {
            try {
                setFavorite(validPokemonId, isFavorite)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                effectChannel.send(PokemonDetailEffect.FavoriteUpdateFailed)
            }
        }
    }
}
