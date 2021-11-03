package com.nasser.pokedexlsi.data.network

import androidx.lifecycle.LiveData
import com.nasser.pokedexlsi.data.entity.Pokemon
import retrofit2.http.GET
import retrofit2.http.Path

interface PokeApiServices {
    @GET("pokemon/{key}")
    suspend fun searchPokemon(@Path("key")key: String): Pokemon
}