package com.jnasser.pokedex.feature.pokemonlist.di

import com.jnasser.pokedex.core.domain.usecase.GetPagedPokemonUseCase
import com.jnasser.pokedex.core.domain.usecase.ObserveCatalogStatusUseCase
import com.jnasser.pokedex.core.domain.usecase.SetFavoriteUseCase
import com.jnasser.pokedex.feature.pokemonlist.PokemonListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val pokemonListModule = module {
    factory { GetPagedPokemonUseCase(get()) }
    factory { ObserveCatalogStatusUseCase(get()) }
    factory { SetFavoriteUseCase(get()) }
    viewModelOf(::PokemonListViewModel)
}
