package com.jnasser.pokedex.core.database.entity

import androidx.room.TypeConverter

class DatabaseTypeConverters {
    @TypeConverter
    fun syncStateToString(value: CatalogSyncStateEntity): String = value.name

    @TypeConverter
    fun stringToSyncState(value: String): CatalogSyncStateEntity =
        CatalogSyncStateEntity.valueOf(value)

    @TypeConverter
    fun syncFailureToString(value: CatalogSyncFailureEntity?): String? = value?.name

    @TypeConverter
    fun stringToSyncFailure(value: String?): CatalogSyncFailureEntity? =
        value?.let(CatalogSyncFailureEntity::valueOf)
}
