package com.jnasser.pokedex.core.network.datasource

import com.jnasser.pokedex.core.network.PokeApiService
import com.jnasser.pokedex.core.model.PokemonCatalogScope
import com.jnasser.pokedex.core.network.dto.PokemonDto
import com.jnasser.pokedex.core.network.model.RemotePokemon
import com.jnasser.pokedex.core.network.model.RemotePokemonAbility
import com.jnasser.pokedex.core.network.model.RemotePokemonCatalog
import com.jnasser.pokedex.core.network.model.RemotePokemonStat
import com.jnasser.pokedex.core.network.model.RemotePokemonType
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import retrofit2.HttpException

class RetrofitPokemonRemoteDataSource(
    private val service: PokeApiService,
    private val catalogSize: Int = PokemonCatalogScope.EXPECTED_POKEMON_COUNT,
    private val maxConcurrentRequests: Int = MAX_CONCURRENT_DETAIL_REQUESTS,
) : PokemonRemoteDataSource {
    init {
        require(catalogSize > 0)
        require(maxConcurrentRequests > 0)
    }

    override suspend fun fetchCatalog(): RemotePokemonCatalog =
        try {
            fetchAndValidateCatalog()
        } catch (error: CancellationException) {
            throw error
        } catch (error: PokemonRemoteException) {
            throw error
        } catch (error: HttpException) {
            throw PokemonRemoteException.Server(error)
        } catch (error: IOException) {
            throw PokemonRemoteException.Network(error)
        } catch (error: Throwable) {
            throw PokemonRemoteException.Unexpected(error)
        }

    private suspend fun fetchAndValidateCatalog(): RemotePokemonCatalog = coroutineScope {
        val resources = service.getPokemonList(
            limit = catalogSize,
            offset = FIRST_POKEMON_OFFSET,
        ).results

        if (resources.size != catalogSize || resources.map { it.name }.distinct().size != catalogSize) {
            throw PokemonRemoteException.IncompleteResponse()
        }

        val semaphore = Semaphore(maxConcurrentRequests)
        val pokemon = resources
            .map { resource ->
                async {
                    semaphore.withPermit {
                        service.getPokemon(resource.name).toRemotePokemon()
                    }
                }
            }
            .awaitAll()
            .sortedBy(RemotePokemon::id)

        val expectedIds = (
            PokemonCatalogScope.FIRST_POKEMON_ID until
                PokemonCatalogScope.FIRST_POKEMON_ID + catalogSize
            ).toSet()
        if (pokemon.map(RemotePokemon::id).toSet() != expectedIds) {
            throw PokemonRemoteException.IncompleteResponse()
        }

        RemotePokemonCatalog(
            expectedPokemonCount = catalogSize,
            pokemon = pokemon,
        )
    }

    private fun PokemonDto.toRemotePokemon(): RemotePokemon {
        if (!isDefault || name.isBlank() || types.isEmpty() || stats.isEmpty()) {
            throw PokemonRemoteException.IncompleteResponse()
        }

        return RemotePokemon(
            id = id,
            name = name,
            imageUrl = sprites.other?.officialArtwork?.frontDefault ?: sprites.frontDefault,
            heightDecimetres = height,
            weightHectograms = weight,
            types = types
                .sortedBy { it.slot }
                .map { RemotePokemonType(slot = it.slot, name = it.type.name) },
            abilities = abilities
                .sortedBy { it.slot }
                .map {
                    RemotePokemonAbility(
                        slot = it.slot,
                        name = it.ability.name,
                        isHidden = it.isHidden,
                    )
                },
            stats = stats.mapIndexed { index, stat ->
                RemotePokemonStat(
                    slot = index + 1,
                    name = stat.stat.name,
                    baseValue = stat.baseStat,
                )
            },
        )
    }

    companion object {
        private const val FIRST_POKEMON_OFFSET = 0
        private const val MAX_CONCURRENT_DETAIL_REQUESTS = 8
    }
}
