package com.jnasser.pokedex

import android.app.Application
import com.jnasser.pokedex.core.data.di.dataModule
import com.jnasser.pokedex.core.database.di.databaseModule
import com.jnasser.pokedex.core.network.di.networkModule
import com.jnasser.pokedex.di.appModule
import com.jnasser.pokedex.feature.pokemonlist.di.pokemonListModule
import com.jnasser.pokedex.sync.CatalogSyncCoordinator
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PokedexApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val koinApplication = startKoin {
            androidContext(this@PokedexApplication)
            modules(
                databaseModule,
                networkModule,
                dataModule,
                appModule,
                pokemonListModule,
            )
        }
        koinApplication.koin.get<CatalogSyncCoordinator>().synchronize()
    }
}
