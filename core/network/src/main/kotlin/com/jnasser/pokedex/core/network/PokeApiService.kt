package com.jnasser.pokedex.core.network

import com.jnasser.pokedex.core.network.dto.PokemonDto
import com.jnasser.pokedex.core.network.dto.PokemonListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): PokemonListResponseDto

    @GET("pokemon/{name}")
    suspend fun getPokemon(
        @Path("name") name: String,
    ): PokemonDto
}
