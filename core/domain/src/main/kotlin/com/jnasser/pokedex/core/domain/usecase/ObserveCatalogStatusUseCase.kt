package com.jnasser.pokedex.core.domain.usecase

import com.jnasser.pokedex.core.domain.repository.PokemonRepository
import com.jnasser.pokedex.core.model.CatalogStatus
import kotlinx.coroutines.flow.Flow

class ObserveCatalogStatusUseCase(
    private val repository: PokemonRepository,
) {
    operator fun invoke(): Flow<CatalogStatus> = repository.observeCatalogStatus()
}
