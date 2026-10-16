package com.jnasser.pokedex.feature.pokemondetail.di

import com.jnasser.pokedex.feature.pokemondetail.PokemonDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val pokemonDetailModule = module {
    viewModelOf(::PokemonDetailViewModel)
}
