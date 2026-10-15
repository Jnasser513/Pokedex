package com.jnasser.pokedex.di

import com.jnasser.pokedex.core.domain.usecase.SynchronizePokemonCatalogUseCase
import com.jnasser.pokedex.sync.CatalogSyncCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single { SynchronizePokemonCatalogUseCase(get()) }
    single(named(APPLICATION_SCOPE)) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
    single {
        CatalogSyncCoordinator(
            synchronizeCatalog = get(),
            applicationScope = get(named(APPLICATION_SCOPE)),
        )
    }
}

private const val APPLICATION_SCOPE = "application_scope"
