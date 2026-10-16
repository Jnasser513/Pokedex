package com.jnasser.pokedex.core.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import app.cash.turbine.test
import com.jnasser.pokedex.core.data.datasource.PokemonLocalDataSource
import com.jnasser.pokedex.core.database.entity.CatalogSyncMetadataEntity
import com.jnasser.pokedex.core.database.entity.CatalogSyncStateEntity
import com.jnasser.pokedex.core.database.entity.PokemonAbilityEntity
import com.jnasser.pokedex.core.database.entity.PokemonEntity
import com.jnasser.pokedex.core.database.entity.PokemonStatEntity
import com.jnasser.pokedex.core.database.entity.PokemonTypeEntity
import com.jnasser.pokedex.core.database.model.PokemonCatalogEntry
import com.jnasser.pokedex.core.database.model.PokemonDetailRecord
import com.jnasser.pokedex.core.database.model.PokemonSummaryRecord
import com.jnasser.pokedex.core.model.CatalogSyncResult
import com.jnasser.pokedex.core.model.CatalogSyncState
import com.jnasser.pokedex.core.model.PokemonCatalogScope
import com.jnasser.pokedex.core.model.SyncFailure
import com.jnasser.pokedex.core.network.datasource.PokemonRemoteDataSource
import com.jnasser.pokedex.core.network.datasource.PokemonRemoteException
import com.jnasser.pokedex.core.network.model.RemotePokemon
import com.jnasser.pokedex.core.network.model.RemotePokemonAbility
import com.jnasser.pokedex.core.network.model.RemotePokemonCatalog
import com.jnasser.pokedex.core.network.model.RemotePokemonStat
import com.jnasser.pokedex.core.network.model.RemotePokemonType
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineFirstPokemonRepositoryTest {
    @Test
    fun `successful sync stores the complete catalog and metadata`() = runTest {
        val local = FakePokemonLocalDataSource()
        val remote = FakePokemonRemoteDataSource { completePokemonCatalog() }
        val times = listOf(1_000L, 2_000L).iterator()
        val repository = OfflineFirstPokemonRepository(remote, local) { times.next() }

        val result = repository.synchronizeCatalog()

        assertEquals(
            CatalogSyncResult.Success(PokemonCatalogScope.EXPECTED_POKEMON_COUNT),
            result,
        )
        assertEquals(PokemonCatalogScope.EXPECTED_POKEMON_COUNT, local.persistedEntries.size)
        assertEquals(CatalogSyncStateEntity.SYNCING, local.metadataUpdates.first().state)
        assertEquals(CatalogSyncStateEntity.COMPLETE, local.metadataUpdates.last().state)
        assertEquals(2_000L, local.metadataUpdates.last().lastSuccessfulSyncEpochMillis)
    }

    @Test
    fun `remote failure keeps local catalog and favorites`() = runTest {
        val local = FakePokemonLocalDataSource(storedPokemonCount = 12).apply {
            favoriteIds += setOf(1, 7, 12)
        }
        val originalFavorites = local.favoriteIds.toSet()
        val remote = FakePokemonRemoteDataSource {
            throw PokemonRemoteException.Network(IOException("offline"))
        }
        val repository = OfflineFirstPokemonRepository(remote, local) { 1_000L }

        val result = repository.synchronizeCatalog()

        assertEquals(
            CatalogSyncResult.Failure(
                reason = SyncFailure.NETWORK,
                hasUsableLocalData = true,
            ),
            result,
        )
        assertTrue(local.persistedEntries.isEmpty())
        assertEquals(originalFavorites, local.favoriteIds)
        assertEquals(CatalogSyncStateEntity.FAILED, local.metadataUpdates.last().state)
    }

    @Test
    fun `storage failure does not report remote data as persisted`() = runTest {
        val local = FakePokemonLocalDataSource(storedPokemonCount = 4).apply {
            failCatalogWrite = true
        }
        val repository = OfflineFirstPokemonRepository(
            remoteDataSource = FakePokemonRemoteDataSource { completePokemonCatalog() },
            localDataSource = local,
            currentTimeMillis = { 1_000L },
        )

        val result = repository.synchronizeCatalog()

        assertEquals(
            CatalogSyncResult.Failure(
                reason = SyncFailure.STORAGE,
                hasUsableLocalData = true,
            ),
            result,
        )
        assertTrue(local.persistedEntries.isEmpty())
        assertEquals(CatalogSyncStateEntity.FAILED, local.metadataUpdates.last().state)
    }

    @Test
    fun `catalog status is derived from local flows`() = runTest {
        val local = FakePokemonLocalDataSource(storedPokemonCount = 8).apply {
            metadataFlow.value = CatalogSyncMetadataEntity(
                expectedPokemonCount = PokemonCatalogScope.EXPECTED_POKEMON_COUNT,
                synchronizedPokemonCount = 8,
                lastAttemptEpochMillis = 2_000L,
                lastSuccessfulSyncEpochMillis = 1_000L,
                state = CatalogSyncStateEntity.SYNCING,
                failureReason = null,
            )
        }
        val repository = OfflineFirstPokemonRepository(
            remoteDataSource = FakePokemonRemoteDataSource { completePokemonCatalog() },
            localDataSource = local,
        )

        val status = repository.observeCatalogStatus().first()

        assertEquals(8, status.storedPokemonCount)
        assertEquals(PokemonCatalogScope.EXPECTED_POKEMON_COUNT, status.expectedPokemonCount)
        assertTrue(status.hasUsableData)
        assertFalse(status.isComplete)
        assertEquals(CatalogSyncState.Syncing, status.syncState)
    }

    @Test
    fun `pokemon detail is observed and mapped only from the local source`() = runTest {
        val local = FakePokemonLocalDataSource().apply {
            detailFlow.value = pokemonDetailRecord(id = 6, isFavorite = true)
        }
        val repository = OfflineFirstPokemonRepository(
            remoteDataSource = FakePokemonRemoteDataSource {
                error("detail observation must not call the remote source")
            },
            localDataSource = local,
        )

        val detail = repository.observePokemon(6).first()

        requireNotNull(detail)
        assertEquals(6, detail.id)
        assertEquals("charizard", detail.name)
        assertEquals(listOf("fire", "flying"), detail.types.map { it.name })
        assertEquals(listOf("blaze", "solar-power"), detail.abilities.map { it.name })
        assertEquals(listOf("hp", "attack"), detail.stats.map { it.name })
        assertTrue(detail.isFavorite)
    }

    @Test
    fun `favorites are mapped from local source and removal stays reactive`() = runTest {
        val local = FakePokemonLocalDataSource().apply {
            favoritesFlow.value = listOf(pokemonSummaryRecord(id = 6, name = "charizard"))
        }
        val repository = OfflineFirstPokemonRepository(
            remoteDataSource = FakePokemonRemoteDataSource {
                error("favorites observation must not call the remote source")
            },
            localDataSource = local,
            currentTimeMillis = { 3_000L },
        )

        repository.observeFavorites().test {
            val initialFavorites = awaitItem()
            assertEquals(listOf(6), initialFavorites.map { it.id })
            assertEquals(listOf("fire", "flying"), initialFavorites.single().types.map { it.name })
            assertTrue(initialFavorites.single().isFavorite)

            repository.setFavorite(id = 6, isFavorite = false)

            assertTrue(awaitItem().isEmpty())
            assertEquals(Triple(6, false, 3_000L), local.lastFavoriteChange)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private class FakePokemonRemoteDataSource(
    private val fetch: suspend () -> RemotePokemonCatalog,
) : PokemonRemoteDataSource {
    override suspend fun fetchCatalog(): RemotePokemonCatalog = fetch()
}

private class FakePokemonLocalDataSource(
    storedPokemonCount: Int = 0,
) : PokemonLocalDataSource {
    val countFlow = MutableStateFlow(storedPokemonCount)
    val metadataFlow = MutableStateFlow<CatalogSyncMetadataEntity?>(null)
    val detailFlow = MutableStateFlow<PokemonDetailRecord?>(null)
    val favoritesFlow = MutableStateFlow<List<PokemonSummaryRecord>>(emptyList())
    val metadataUpdates = mutableListOf<CatalogSyncMetadataEntity>()
    val favoriteIds = mutableSetOf<Int>()
    var persistedEntries: List<PokemonCatalogEntry> = emptyList()
    var failCatalogWrite = false
    var lastFavoriteChange: Triple<Int, Boolean, Long>? = null

    override fun pagingSource(query: String): PagingSource<Int, PokemonSummaryRecord> =
        EmptyPokemonPagingSource()

    override fun observePokemon(id: Int): Flow<PokemonDetailRecord?> =
        detailFlow

    override fun observeFavorites(): Flow<List<PokemonSummaryRecord>> =
        favoritesFlow

    override fun observePokemonCount(): Flow<Int> = countFlow

    override fun observeSyncMetadata(): Flow<CatalogSyncMetadataEntity?> = metadataFlow

    override suspend fun pokemonCount(): Int = countFlow.value

    override suspend fun syncMetadata(): CatalogSyncMetadataEntity? = metadataFlow.value

    override suspend fun updateSyncMetadata(metadata: CatalogSyncMetadataEntity) {
        metadataUpdates += metadata
        metadataFlow.value = metadata
    }

    override suspend fun replaceCatalog(
        entries: List<PokemonCatalogEntry>,
        metadata: CatalogSyncMetadataEntity,
    ) {
        if (failCatalogWrite) throw IOException("database unavailable")
        persistedEntries = entries
        countFlow.value = entries.size
        updateSyncMetadata(metadata)
    }

    override suspend fun setFavorite(
        pokemonId: Int,
        isFavorite: Boolean,
        changedAtEpochMillis: Long,
    ) {
        lastFavoriteChange = Triple(pokemonId, isFavorite, changedAtEpochMillis)
        if (isFavorite) favoriteIds += pokemonId else favoriteIds -= pokemonId
        if (!isFavorite) {
            favoritesFlow.value = favoritesFlow.value.filterNot { it.pokemon.id == pokemonId }
        }
    }
}

private fun pokemonSummaryRecord(
    id: Int,
    name: String,
) = PokemonSummaryRecord(
    pokemon = PokemonEntity(
        id = id,
        name = name,
        imageUrl = "https://images.test/$id.png",
        heightDecimetres = 17,
        weightHectograms = 905,
        detailsComplete = true,
    ),
    types = listOf(
        PokemonTypeEntity(pokemonId = id, slot = 2, name = "flying"),
        PokemonTypeEntity(pokemonId = id, slot = 1, name = "fire"),
    ),
    isFavorite = true,
)

private fun pokemonDetailRecord(
    id: Int,
    isFavorite: Boolean,
) = PokemonDetailRecord(
    pokemon = PokemonEntity(
        id = id,
        name = "charizard",
        imageUrl = "https://images.test/$id.png",
        heightDecimetres = 17,
        weightHectograms = 905,
        detailsComplete = true,
    ),
    types = listOf(
        PokemonTypeEntity(pokemonId = id, slot = 2, name = "flying"),
        PokemonTypeEntity(pokemonId = id, slot = 1, name = "fire"),
    ),
    abilities = listOf(
        PokemonAbilityEntity(
            pokemonId = id,
            slot = 2,
            name = "solar-power",
            isHidden = true,
        ),
        PokemonAbilityEntity(
            pokemonId = id,
            slot = 1,
            name = "blaze",
            isHidden = false,
        ),
    ),
    stats = listOf(
        PokemonStatEntity(pokemonId = id, slot = 2, name = "attack", baseValue = 84),
        PokemonStatEntity(pokemonId = id, slot = 1, name = "hp", baseValue = 78),
    ),
    isFavorite = isFavorite,
)

private class EmptyPokemonPagingSource : PagingSource<Int, PokemonSummaryRecord>() {
    override suspend fun load(
        params: LoadParams<Int>,
    ): LoadResult<Int, PokemonSummaryRecord> = LoadResult.Page(
        data = emptyList(),
        prevKey = null,
        nextKey = null,
    )

    override fun getRefreshKey(state: PagingState<Int, PokemonSummaryRecord>): Int? = null
}

private fun completePokemonCatalog(): RemotePokemonCatalog =
    RemotePokemonCatalog(
        expectedPokemonCount = PokemonCatalogScope.EXPECTED_POKEMON_COUNT,
        pokemon = (1..PokemonCatalogScope.EXPECTED_POKEMON_COUNT).map { id ->
            RemotePokemon(
                id = id,
                name = "pokemon-$id",
                imageUrl = "https://images.test/$id.png",
                heightDecimetres = 10,
                weightHectograms = 100,
                types = listOf(RemotePokemonType(slot = 1, name = "normal")),
                abilities = listOf(
                    RemotePokemonAbility(
                        slot = 1,
                        name = "ability-$id",
                        isHidden = false,
                    )
                ),
                stats = listOf(RemotePokemonStat(slot = 1, name = "hp", baseValue = 50)),
            )
        },
    )
