package com.jnasser.pokedex.core.domain.usecase

import com.jnasser.pokedex.core.domain.repository.PokemonRepository
import com.jnasser.pokedex.core.model.CatalogSyncResult

class SynchronizePokemonCatalogUseCase(
    private val repository: PokemonRepository,
) {
    suspend operator fun invoke(): CatalogSyncResult = repository.synchronizeCatalog()
}
