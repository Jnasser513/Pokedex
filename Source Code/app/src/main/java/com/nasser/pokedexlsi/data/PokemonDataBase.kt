package com.nasser.pokedexlsi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nasser.pokedexlsi.data.dao.PokemonDao
import com.nasser.pokedexlsi.data.entity.Pokemon
import com.nasser.pokedexlsi.data.entity.PokemonType

@Database(version = 1, entities = [Pokemon::class, PokemonType::class], exportSchema = true)
abstract class PokemonDataBase: RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao

    companion object {
        @Volatile
        private var INSTANCE: PokemonDataBase? = null

        fun getDatabase(context: Context): PokemonDataBase {
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context,
                    PokemonDataBase::class.java, "pokedexDb"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}