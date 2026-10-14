package com.jnasser.pokedex.core.domain.usecase

import androidx.paging.PagingData
import com.jnasser.pokedex.core.domain.repository.PokemonRepository
import com.jnasser.pokedex.core.model.PokemonSummary
import java.util.Locale
import kotlinx.coroutines.flow.Flow

class GetPagedPokemonUseCase(
    private val repository: PokemonRepository,
) {
    operator fun invoke(query: String): Flow<PagingData<PokemonSummary>> =
        repository.getPagedPokemon(query.trim().lowercase(Locale.ROOT))
}
