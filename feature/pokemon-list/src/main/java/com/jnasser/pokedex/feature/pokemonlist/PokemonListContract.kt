package com.jnasser.pokedex.feature.pokemonlist

import com.jnasser.pokedex.core.model.SyncFailure

data class PokemonListUiState(
    val searchQuery: String = "",
    val storedPokemonCount: Int = 0,
    val hasContent: Boolean = false,
    val isInitialLoading: Boolean = true,
    val initialError: SyncFailure? = null,
    val isRefreshing: Boolean = false,
    val refreshError: SyncFailure? = null,
)

sealed interface PokemonListAction {
    data class SearchQueryChanged(val query: String) : PokemonListAction

    data object ClearSearch : PokemonListAction

    data object RetrySynchronization : PokemonListAction

    data class FavoriteChanged(
        val pokemonId: Int,
        val isFavorite: Boolean,
    ) : PokemonListAction
}

sealed interface PokemonListEffect {
    data object FavoriteUpdateFailed : PokemonListEffect
}
