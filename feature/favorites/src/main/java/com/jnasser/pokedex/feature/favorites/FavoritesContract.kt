package com.jnasser.pokedex.feature.favorites

import com.jnasser.pokedex.core.model.PokemonSummary

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState

    data object Empty : FavoritesUiState

    data class Content(
        val pokemon: List<PokemonSummary>,
    ) : FavoritesUiState

    data object Error : FavoritesUiState
}

sealed interface FavoritesAction {
    data object Retry : FavoritesAction

    data class RemoveFavorite(
        val pokemonId: Int,
    ) : FavoritesAction
}

sealed interface FavoritesEffect {
    data object FavoriteUpdateFailed : FavoritesEffect
}
