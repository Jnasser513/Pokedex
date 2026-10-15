package com.jnasser.pokedex.core.network.di

import com.jnasser.pokedex.core.network.PokeApiService
import com.jnasser.pokedex.core.network.datasource.PokemonRemoteDataSource
import com.jnasser.pokedex.core.network.datasource.RetrofitPokemonRemoteDataSource
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
    single {
        OkHttpClient.Builder()
            .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(POKE_API_BASE_URL)
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory(JSON_MEDIA_TYPE.toMediaType()))
            .build()
    }
    single { get<Retrofit>().create(PokeApiService::class.java) }
    single<PokemonRemoteDataSource> { RetrofitPokemonRemoteDataSource(get()) }
}

private const val POKE_API_BASE_URL = "https://pokeapi.co/api/v2/"
private const val JSON_MEDIA_TYPE = "application/json"
private const val NETWORK_TIMEOUT_SECONDS = 20L
