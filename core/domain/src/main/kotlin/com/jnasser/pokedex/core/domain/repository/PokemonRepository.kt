package com.jnasser.pokedex.core.domain.repository

import androidx.paging.PagingData
import com.jnasser.pokedex.core.model.CatalogStatus
import com.jnasser.pokedex.core.model.CatalogSyncResult
import com.jnasser.pokedex.core.model.PokemonDetail
import com.jnasser.pokedex.core.model.PokemonSummary
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    fun getPagedPokemon(query: String): Flow<PagingData<PokemonSummary>>

    fun observePokemon(id: Int): Flow<PokemonDetail?>

    fun observeFavorites(): Flow<List<PokemonSummary>>

    fun observeCatalogStatus(): Flow<CatalogStatus>

    suspend fun setFavorite(id: Int, isFavorite: Boolean)

    suspend fun synchronizeCatalog(): CatalogSyncResult
}
