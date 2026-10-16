package com.jnasser.pokedex.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_sync_metadata")
data class CatalogSyncMetadataEntity(
    @PrimaryKey
    val id: String = POKEMON_CATALOG_ID,
    @ColumnInfo(name = "expected_pokemon_count")
    val expectedPokemonCount: Int?,
    @ColumnInfo(name = "synchronized_pokemon_count")
    val synchronizedPokemonCount: Int,
    @ColumnInfo(name = "last_attempt_epoch_millis")
    val lastAttemptEpochMillis: Long?,
    @ColumnInfo(name = "last_successful_sync_epoch_millis")
    val lastSuccessfulSyncEpochMillis: Long?,
    val state: CatalogSyncStateEntity,
    @ColumnInfo(name = "failure_reason")
    val failureReason: CatalogSyncFailureEntity?,
) {
    companion object {
        const val POKEMON_CATALOG_ID = "pokemon_catalog"
    }
}

enum class CatalogSyncStateEntity {
    NOT_STARTED,
    SYNCING,
    COMPLETE,
    FAILED,
}

enum class CatalogSyncFailureEntity {
    NETWORK,
    SERVER,
    INCOMPLETE_RESPONSE,
    STORAGE,
    UNKNOWN,
}
