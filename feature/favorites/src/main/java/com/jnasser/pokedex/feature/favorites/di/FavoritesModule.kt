package com.jnasser.pokedex.feature.favorites.di

import com.jnasser.pokedex.feature.favorites.FavoritesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val favoritesModule = module {
    viewModelOf(::FavoritesViewModel)
}
