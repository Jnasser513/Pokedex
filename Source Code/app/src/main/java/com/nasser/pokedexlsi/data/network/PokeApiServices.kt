package com.nasser.pokedexlsi.data.network

import com.nasser.pokedexlsi.data.entity.Pokemon
import com.nasser.pokedexlsi.data.entity.Pokemon1
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiServices {
    @GET("pokemon/{key}")
    suspend fun searchPokemon(@Path("key")key: String): Pokemon
    @GET("pokemon")
    fun getPokemonList(@Query("limit") limit: Int, @Query("offset") offset: Int): Call<PokeApiResponse>
    @GET("pokemon/{id}")
    fun getPokemonDetails(@Path("id") id: Int): Call<Pokemon1>
}