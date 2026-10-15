package com.jnasser.pokedex.core.data.datasource

import androidx.paging.PagingSource
import com.jnasser.pokedex.core.database.entity.CatalogSyncMetadataEntity
import com.jnasser.pokedex.core.database.model.PokemonCatalogEntry
import com.jnasser.pokedex.core.database.model.PokemonDetailRecord
import com.jnasser.pokedex.core.database.model.PokemonSummaryRecord
import kotlinx.coroutines.flow.Flow

interface PokemonLocalDataSource {
    fun pagingSource(query: String): PagingSource<Int, PokemonSummaryRecord>

    fun observePokemon(id: Int): Flow<PokemonDetailRecord?>

    fun observeFavorites(): Flow<List<PokemonSummaryRecord>>

    fun observePokemonCount(): Flow<Int>

    fun observeSyncMetadata(): Flow<CatalogSyncMetadataEntity?>

    suspend fun pokemonCount(): Int

    suspend fun syncMetadata(): CatalogSyncMetadataEntity?

    suspend fun updateSyncMetadata(metadata: CatalogSyncMetadataEntity)

    suspend fun replaceCatalog(
        entries: List<PokemonCatalogEntry>,
        metadata: CatalogSyncMetadataEntity,
    )

    suspend fun setFavorite(
        pokemonId: Int,
        isFavorite: Boolean,
        changedAtEpochMillis: Long,
    )
}
