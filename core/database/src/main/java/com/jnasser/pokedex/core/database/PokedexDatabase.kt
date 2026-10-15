package com.jnasser.pokedex.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jnasser.pokedex.core.database.dao.CatalogSyncMetadataDao
import com.jnasser.pokedex.core.database.dao.FavoriteDao
import com.jnasser.pokedex.core.database.dao.PokemonDao
import com.jnasser.pokedex.core.database.entity.CatalogSyncMetadataEntity
import com.jnasser.pokedex.core.database.entity.DatabaseTypeConverters
import com.jnasser.pokedex.core.database.entity.FavoriteEntity
import com.jnasser.pokedex.core.database.entity.PokemonAbilityEntity
import com.jnasser.pokedex.core.database.entity.PokemonEntity
import com.jnasser.pokedex.core.database.entity.PokemonStatEntity
import com.jnasser.pokedex.core.database.entity.PokemonTypeEntity

@Database(
    entities = [
        PokemonEntity::class,
        PokemonTypeEntity::class,
        PokemonAbilityEntity::class,
        PokemonStatEntity::class,
        FavoriteEntity::class,
        CatalogSyncMetadataEntity::class,
    ],
    version = PokedexDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class PokedexDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao

    abstract fun favoriteDao(): FavoriteDao

    abstract fun catalogSyncMetadataDao(): CatalogSyncMetadataDao

    companion object {
        const val VERSION = 1
        const val NAME = "pokedex.db"

        fun create(context: Context): PokedexDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                PokedexDatabase::class.java,
                NAME,
            ).build()
    }
}
