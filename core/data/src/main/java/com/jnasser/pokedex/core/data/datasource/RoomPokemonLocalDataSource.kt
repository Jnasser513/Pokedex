package com.jnasser.pokedex.core.data.datasource

import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.jnasser.pokedex.core.database.PokedexDatabase
import com.jnasser.pokedex.core.database.entity.CatalogSyncMetadataEntity
import com.jnasser.pokedex.core.database.entity.FavoriteEntity
import com.jnasser.pokedex.core.database.model.PokemonCatalogEntry
import com.jnasser.pokedex.core.database.model.PokemonDetailRecord
import com.jnasser.pokedex.core.database.model.PokemonSummaryRecord
import kotlinx.coroutines.flow.Flow

class RoomPokemonLocalDataSource(
    private val database: PokedexDatabase,
) : PokemonLocalDataSource {
    private val pokemonDao = database.pokemonDao()
    private val favoriteDao = database.favoriteDao()
    private val syncMetadataDao = database.catalogSyncMetadataDao()

    override fun pagingSource(query: String): PagingSource<Int, PokemonSummaryRecord> =
        pokemonDao.pagingSource(query)

    override fun observePokemon(id: Int): Flow<PokemonDetailRecord?> = pokemonDao.observePokemon(id)

    override fun observeFavorites(): Flow<List<PokemonSummaryRecord>> = pokemonDao.observeFavorites()

    override fun observePokemonCount(): Flow<Int> = pokemonDao.observePokemonCount()

    override fun observeSyncMetadata(): Flow<CatalogSyncMetadataEntity?> = syncMetadataDao.observe()

    override suspend fun pokemonCount(): Int = pokemonDao.pokemonCount()

    override suspend fun syncMetadata(): CatalogSyncMetadataEntity? = syncMetadataDao.get()

    override suspend fun updateSyncMetadata(metadata: CatalogSyncMetadataEntity) {
        syncMetadataDao.upsert(metadata)
    }

    override suspend fun replaceCatalog(
        entries: List<PokemonCatalogEntry>,
        metadata: CatalogSyncMetadataEntity,
    ) {
        database.withTransaction {
            pokemonDao.upsertCatalog(entries)
            syncMetadataDao.upsert(metadata)
        }
    }

    override suspend fun setFavorite(
        pokemonId: Int,
        isFavorite: Boolean,
        changedAtEpochMillis: Long,
    ) {
        if (isFavorite) {
            favoriteDao.add(
                FavoriteEntity(
                    pokemonId = pokemonId,
                    createdAtEpochMillis = changedAtEpochMillis,
                )
            )
        } else {
            favoriteDao.remove(pokemonId)
        }
    }
}
