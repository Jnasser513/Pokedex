package com.nasser.pokedexlsi.repository

import androidx.lifecycle.LiveData
import com.nasser.pokedexlsi.data.dao.PokemonDao
import com.nasser.pokedexlsi.data.entity.Pokemon
import com.nasser.pokedexlsi.data.network.PokeAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PokemonRepository(private val API: PokeAPI, private val pokemonDao: PokemonDao) {
    /**
     * Busca un pokemon
     * @param key [String] id
     * @return [String] JSON con la información de pokemon
     */
    suspend fun search(key: String) = withContext(Dispatchers.IO) {
        //Si se encuentra en la base de datos
        var pokemon = pokemonDao.search(key)
        //Si no busca en la red y lo inserta en la base de datos
        if (pokemon == null) {
            pokemon = API.service.searchPokemon(key)
            pokemonDao.insertPokemonWithType(pokemon)
        }
        pokemon
    }

    /*
    suspend fun findAll() = withContext(Dispatchers.IO){
        for(i in 1..150){
            var pokemon = pokemonDao.search(i.toString())
            if(pokemon == null) {
                pokemon = API.service.searchPokemon(i.toString())
                pokemonDao.insertPokemonWithType(pokemon)
            }
            return@withContext pokemon
        }
    }*/
}