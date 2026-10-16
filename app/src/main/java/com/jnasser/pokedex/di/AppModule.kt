package com.jnasser.pokedex.di

import com.jnasser.pokedex.core.domain.usecase.GetPagedPokemonUseCase
import com.jnasser.pokedex.core.domain.usecase.ObserveCatalogStatusUseCase
import com.jnasser.pokedex.core.domain.usecase.ObservePokemonDetailUseCase
import com.jnasser.pokedex.core.domain.usecase.SetFavoriteUseCase
import com.jnasser.pokedex.core.domain.usecase.SynchronizePokemonCatalogUseCase
import com.jnasser.pokedex.sync.CatalogSyncCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::GetPagedPokemonUseCase)
    singleOf(::ObserveCatalogStatusUseCase)
    singleOf(::ObservePokemonDetailUseCase)
    singleOf(::SetFavoriteUseCase)
    singleOf(::SynchronizePokemonCatalogUseCase)
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
