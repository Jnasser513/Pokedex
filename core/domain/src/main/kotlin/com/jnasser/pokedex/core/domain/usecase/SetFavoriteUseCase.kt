package com.jnasser.pokedex.core.domain.usecase

import com.jnasser.pokedex.core.domain.repository.PokemonRepository

class SetFavoriteUseCase(
    private val repository: PokemonRepository,
) {
    suspend operator fun invoke(id: Int, isFavorite: Boolean) {
        require(id > 0) { "Pokemon id must be positive" }
        repository.setFavorite(id, isFavorite)
    }
}
