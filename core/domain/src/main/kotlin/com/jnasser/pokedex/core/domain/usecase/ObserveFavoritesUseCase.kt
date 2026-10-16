package com.jnasser.pokedex.core.domain.usecase

import com.jnasser.pokedex.core.domain.repository.PokemonRepository
import com.jnasser.pokedex.core.model.PokemonSummary
import kotlinx.coroutines.flow.Flow

class ObserveFavoritesUseCase(
    private val repository: PokemonRepository,
) {
    operator fun invoke(): Flow<List<PokemonSummary>> = repository.observeFavorites()
}
