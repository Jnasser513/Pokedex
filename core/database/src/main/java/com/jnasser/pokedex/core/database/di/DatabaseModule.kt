package com.jnasser.pokedex.core.database.di

import com.jnasser.pokedex.core.database.PokedexDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { PokedexDatabase.create(androidContext()) }
    single { get<PokedexDatabase>().pokemonDao() }
    single { get<PokedexDatabase>().favoriteDao() }
    single { get<PokedexDatabase>().catalogSyncMetadataDao() }
}
