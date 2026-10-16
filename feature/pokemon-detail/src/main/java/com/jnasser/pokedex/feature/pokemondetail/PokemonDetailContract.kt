package com.jnasser.pokedex.feature.pokemondetail

import com.jnasser.pokedex.core.model.PokemonDetail

sealed interface PokemonDetailUiState {
    data object Loading : PokemonDetailUiState

    data class Content(
        val pokemon: PokemonDetail,
    ) : PokemonDetailUiState

    data object NotFound : PokemonDetailUiState

    data object InvalidPokemonId : PokemonDetailUiState

    data object Error : PokemonDetailUiState
}

sealed interface PokemonDetailAction {
    data object Retry : PokemonDetailAction

    data class FavoriteChanged(
        val isFavorite: Boolean,
    ) : PokemonDetailAction
}

sealed interface PokemonDetailEffect {
    data object FavoriteUpdateFailed : PokemonDetailEffect
}
