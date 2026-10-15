package com.jnasser.pokedex.core.network.datasource

import com.jnasser.pokedex.core.network.model.RemotePokemonCatalog

interface PokemonRemoteDataSource {
    suspend fun fetchCatalog(): RemotePokemonCatalog
}

sealed class PokemonRemoteException(
    cause: Throwable? = null,
) : Exception(cause) {
    class Network(cause: Throwable) : PokemonRemoteException(cause)

    class Server(cause: Throwable) : PokemonRemoteException(cause)

    class IncompleteResponse(cause: Throwable? = null) : PokemonRemoteException(cause)

    class Unexpected(cause: Throwable) : PokemonRemoteException(cause)
}
