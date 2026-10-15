package com.jnasser.pokedex.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jnasser.pokedex.core.database.entity.CatalogSyncMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogSyncMetadataDao {
    @Upsert
    suspend fun upsert(metadata: CatalogSyncMetadataEntity)

    @Query(
        """
        SELECT *
        FROM catalog_sync_metadata
        WHERE id = :catalogId
        """
    )
    fun observe(
        catalogId: String = CatalogSyncMetadataEntity.POKEMON_CATALOG_ID,
    ): Flow<CatalogSyncMetadataEntity?>
}
