package com.jnasser.pokedex.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jnasser.pokedex.core.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Upsert
    suspend fun add(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite WHERE pokemon_id = :pokemonId")
    suspend fun remove(pokemonId: Int)

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM favorite
            WHERE pokemon_id = :pokemonId
        )
        """
    )
    fun observeIsFavorite(pokemonId: Int): Flow<Boolean>

    @Query("SELECT pokemon_id FROM favorite ORDER BY created_at_epoch_millis DESC")
    fun observeFavoriteIds(): Flow<List<Int>>
}
