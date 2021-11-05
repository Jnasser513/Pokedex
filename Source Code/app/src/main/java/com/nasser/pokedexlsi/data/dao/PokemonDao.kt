package com.nasser.pokedexlsi.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.nasser.pokedexlsi.data.entity.Pokemon
import com.nasser.pokedexlsi.data.entity.PokemonType
import com.nasser.pokedexlsi.data.entity.PokemonWithType

@Dao
interface PokemonDao{
    @Insert
    suspend fun insert(pokemon: Pokemon)

    @Query("SELECT * FROM pokemons WHERE id = :key or name = :key")
    suspend fun search(key: String): Pokemon?

    @Query("SELECT * FROM pokemons")
    fun findAll(): LiveData<List<Pokemon>>

    @Update
    suspend fun update(pokemon: Pokemon)

    @Delete
    suspend fun delete(pokemon: Pokemon)

    @Insert
    suspend fun insertTypesOfPokemon(types: List<PokemonType>)

    @Transaction
    suspend fun insertPokemonWithType(pokemon: Pokemon){
        insert(pokemon)
        val pokemonTypes =
            pokemon.types.map {
                it.idPokemon = pokemon.id
                it
            }
        insertTypesOfPokemon(pokemonTypes)
    }

    @Transaction
    @Query(value = "SELECT * FROM pokemons")
    fun getPokemonsWithType() : LiveData<List<PokemonWithType>>
}