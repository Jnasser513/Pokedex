package com.nasser.pokedexlsi.repository

import com.nasser.pokedexlsi.data.dao.PokemonDao
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
        var pokemon = pokemonDao.search(key)
        if (pokemon == null) {
            pokemon = API.service.searchPokemon(key)
            pokemonDao.insertPokemonWithType(pokemon)
        }
        pokemon
    }

    fun findAll() = pokemonDao.getPokemonsWithType()
}