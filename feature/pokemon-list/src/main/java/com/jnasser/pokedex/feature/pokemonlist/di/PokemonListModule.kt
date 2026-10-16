package com.jnasser.pokedex.feature.pokemonlist.di

import com.jnasser.pokedex.feature.pokemonlist.PokemonListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val pokemonListModule = module {
    viewModelOf(::PokemonListViewModel)
}
