package com.jnasser.pokedex.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.jnasser.pokedex.core.data.datasource.PokemonLocalDataSource
import com.jnasser.pokedex.core.data.mapper.toDomain
import com.jnasser.pokedex.core.data.mapper.toDomainOrNull
import com.jnasser.pokedex.core.data.mapper.toEntity
import com.jnasser.pokedex.core.database.entity.CatalogSyncFailureEntity
import com.jnasser.pokedex.core.database.entity.CatalogSyncMetadataEntity
import com.jnasser.pokedex.core.database.entity.CatalogSyncStateEntity
import com.jnasser.pokedex.core.domain.repository.PokemonRepository
import com.jnasser.pokedex.core.model.CatalogStatus
import com.jnasser.pokedex.core.model.CatalogSyncResult
import com.jnasser.pokedex.core.model.CatalogSyncState
import com.jnasser.pokedex.core.model.PokemonCatalogScope
import com.jnasser.pokedex.core.model.PokemonDetail
import com.jnasser.pokedex.core.model.PokemonSummary
import com.jnasser.pokedex.core.model.SyncFailure
import com.jnasser.pokedex.core.network.datasource.PokemonRemoteDataSource
import com.jnasser.pokedex.core.network.datasource.PokemonRemoteException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class OfflineFirstPokemonRepository(
    private val remoteDataSource: PokemonRemoteDataSource,
    private val localDataSource: PokemonLocalDataSource,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) : PokemonRepository {
    override fun getPagedPokemon(query: String): Flow<PagingData<PokemonSummary>> =
        Pager(
            config = PagingConfig(
                pageSize = LOCAL_PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { localDataSource.pagingSource(query) },
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override fun observePokemon(id: Int): Flow<PokemonDetail?> =
        localDataSource.observePokemon(id).map { it?.toDomainOrNull() }

    override fun observeFavorites(): Flow<List<PokemonSummary>> =
        localDataSource.observeFavorites().map { records -> records.map { it.toDomain() } }

    override fun observeCatalogStatus(): Flow<CatalogStatus> =
        combine(
            localDataSource.observePokemonCount(),
            localDataSource.observeSyncMetadata(),
        ) { storedCount, metadata ->
            CatalogStatus(
                storedPokemonCount = storedCount,
                expectedPokemonCount = metadata?.expectedPokemonCount,
                lastSuccessfulSyncEpochMillis = metadata?.lastSuccessfulSyncEpochMillis,
                syncState = metadata.toDomainState(),
            )
        }

    override suspend fun setFavorite(id: Int, isFavorite: Boolean) {
        localDataSource.setFavorite(
            pokemonId = id,
            isFavorite = isFavorite,
            changedAtEpochMillis = currentTimeMillis(),
        )
    }

    override suspend fun synchronizeCatalog(): CatalogSyncResult {
        val initialState = readInitialState()
            ?: return CatalogSyncResult.Failure(
                reason = SyncFailure.STORAGE,
                hasUsableLocalData = false,
            )
        val attemptTime = currentTimeMillis()

        try {
            localDataSource.updateSyncMetadata(
                initialState.metadataForAttempt(attemptTime)
            )
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            return CatalogSyncResult.Failure(
                reason = SyncFailure.STORAGE,
                hasUsableLocalData = initialState.storedPokemonCount > 0,
            )
        }

        val remoteCatalog = try {
            remoteDataSource.fetchCatalog()
        } catch (error: CancellationException) {
            throw error
        } catch (error: PokemonRemoteException) {
            return recordFailure(
                failure = error.toDomainFailure(),
                initialState = initialState,
                attemptTime = attemptTime,
            )
        } catch (_: Throwable) {
            return recordFailure(
                failure = SyncFailure.UNKNOWN,
                initialState = initialState,
                attemptTime = attemptTime,
            )
        }

        val entries = remoteCatalog.pokemon.map { it.toEntity() }
        if (
            entries.size != remoteCatalog.expectedPokemonCount ||
            remoteCatalog.expectedPokemonCount != PokemonCatalogScope.EXPECTED_POKEMON_COUNT
        ) {
            return recordFailure(
                failure = SyncFailure.INCOMPLETE_RESPONSE,
                initialState = initialState,
                attemptTime = attemptTime,
            )
        }

        return try {
            val successTime = currentTimeMillis()
            localDataSource.replaceCatalog(
                entries = entries,
                metadata = CatalogSyncMetadataEntity(
                    expectedPokemonCount = remoteCatalog.expectedPokemonCount,
                    synchronizedPokemonCount = entries.size,
                    lastAttemptEpochMillis = attemptTime,
                    lastSuccessfulSyncEpochMillis = successTime,
                    state = CatalogSyncStateEntity.COMPLETE,
                    failureReason = null,
                ),
            )
            CatalogSyncResult.Success(synchronizedPokemonCount = entries.size)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            recordFailure(
                failure = SyncFailure.STORAGE,
                initialState = initialState,
                attemptTime = attemptTime,
            )
        }
    }

    private suspend fun readInitialState(): InitialCatalogState? =
        try {
            InitialCatalogState(
                storedPokemonCount = localDataSource.pokemonCount(),
                metadata = localDataSource.syncMetadata(),
            )
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            null
        }

    private suspend fun recordFailure(
        failure: SyncFailure,
        initialState: InitialCatalogState,
        attemptTime: Long,
    ): CatalogSyncResult {
        val recordedFailure = try {
            localDataSource.updateSyncMetadata(
                CatalogSyncMetadataEntity(
                    expectedPokemonCount = PokemonCatalogScope.EXPECTED_POKEMON_COUNT,
                    synchronizedPokemonCount = initialState.storedPokemonCount,
                    lastAttemptEpochMillis = attemptTime,
                    lastSuccessfulSyncEpochMillis =
                        initialState.metadata?.lastSuccessfulSyncEpochMillis,
                    state = CatalogSyncStateEntity.FAILED,
                    failureReason = failure.toEntity(),
                )
            )
            failure
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            SyncFailure.STORAGE
        }

        return CatalogSyncResult.Failure(
            reason = recordedFailure,
            hasUsableLocalData = initialState.storedPokemonCount > 0,
        )
    }

    private data class InitialCatalogState(
        val storedPokemonCount: Int,
        val metadata: CatalogSyncMetadataEntity?,
    ) {
        fun metadataForAttempt(attemptTime: Long): CatalogSyncMetadataEntity =
            CatalogSyncMetadataEntity(
                expectedPokemonCount = PokemonCatalogScope.EXPECTED_POKEMON_COUNT,
                synchronizedPokemonCount = storedPokemonCount,
                lastAttemptEpochMillis = attemptTime,
                lastSuccessfulSyncEpochMillis = metadata?.lastSuccessfulSyncEpochMillis,
                state = CatalogSyncStateEntity.SYNCING,
                failureReason = null,
            )
    }

    companion object {
        private const val LOCAL_PAGE_SIZE = 20
    }
}

private fun CatalogSyncMetadataEntity?.toDomainState(): CatalogSyncState =
    when (this?.state) {
        null,
        CatalogSyncStateEntity.NOT_STARTED,
        CatalogSyncStateEntity.COMPLETE,
        -> CatalogSyncState.Idle

        CatalogSyncStateEntity.SYNCING -> CatalogSyncState.Syncing
        CatalogSyncStateEntity.FAILED -> CatalogSyncState.Failed(
            reason = failureReason?.toDomain() ?: SyncFailure.UNKNOWN
        )
    }

private fun PokemonRemoteException.toDomainFailure(): SyncFailure =
    when (this) {
        is PokemonRemoteException.Network -> SyncFailure.NETWORK
        is PokemonRemoteException.Server -> SyncFailure.SERVER
        is PokemonRemoteException.IncompleteResponse -> SyncFailure.INCOMPLETE_RESPONSE
        is PokemonRemoteException.Unexpected -> SyncFailure.UNKNOWN
    }

private fun SyncFailure.toEntity(): CatalogSyncFailureEntity =
    when (this) {
        SyncFailure.NETWORK -> CatalogSyncFailureEntity.NETWORK
        SyncFailure.SERVER -> CatalogSyncFailureEntity.SERVER
        SyncFailure.INCOMPLETE_RESPONSE -> CatalogSyncFailureEntity.INCOMPLETE_RESPONSE
        SyncFailure.STORAGE -> CatalogSyncFailureEntity.STORAGE
        SyncFailure.UNKNOWN -> CatalogSyncFailureEntity.UNKNOWN
    }

private fun CatalogSyncFailureEntity.toDomain(): SyncFailure =
    when (this) {
        CatalogSyncFailureEntity.NETWORK -> SyncFailure.NETWORK
        CatalogSyncFailureEntity.SERVER -> SyncFailure.SERVER
        CatalogSyncFailureEntity.INCOMPLETE_RESPONSE -> SyncFailure.INCOMPLETE_RESPONSE
        CatalogSyncFailureEntity.STORAGE -> SyncFailure.STORAGE
        CatalogSyncFailureEntity.UNKNOWN -> SyncFailure.UNKNOWN
    }
