package com.nasser.pokedexlsi

import android.app.Application
import com.nasser.pokedexlsi.data.PokemonDataBase
import com.nasser.pokedexlsi.data.network.PokeAPI
import com.nasser.pokedexlsi.repository.PokemonRepository

class PokedexApplication: Application() {
    private val database by lazy { PokemonDataBase.getDatabase(this) }
    val pokemonRepository by lazy {
        val pokemonDao = database.pokemonDao()
        PokemonRepository(PokeAPI, pokemonDao)
    }

}