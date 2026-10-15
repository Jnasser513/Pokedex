package com.jnasser.pokedex.core.data.di

import com.jnasser.pokedex.core.data.datasource.PokemonLocalDataSource
import com.jnasser.pokedex.core.data.datasource.RoomPokemonLocalDataSource
import com.jnasser.pokedex.core.data.repository.OfflineFirstPokemonRepository
import com.jnasser.pokedex.core.domain.repository.PokemonRepository
import org.koin.dsl.module

val dataModule = module {
    single<PokemonLocalDataSource> { RoomPokemonLocalDataSource(get()) }
    single<PokemonRepository> {
        OfflineFirstPokemonRepository(
            remoteDataSource = get(),
            localDataSource = get(),
        )
    }
}
