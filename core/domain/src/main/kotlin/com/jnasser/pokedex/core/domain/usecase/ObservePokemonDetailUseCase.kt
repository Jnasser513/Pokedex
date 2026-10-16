package com.jnasser.pokedex.core.domain.usecase

import com.jnasser.pokedex.core.domain.repository.PokemonRepository
import com.jnasser.pokedex.core.model.PokemonDetail
import kotlinx.coroutines.flow.Flow

class ObservePokemonDetailUseCase(
    private val repository: PokemonRepository,
) {
    operator fun invoke(id: Int): Flow<PokemonDetail?> {
        require(id > 0) { "Pokemon id must be positive" }
        return repository.observePokemon(id)
    }
}
