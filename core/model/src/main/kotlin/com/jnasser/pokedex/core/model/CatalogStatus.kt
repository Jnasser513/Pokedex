package com.jnasser.pokedex.core.model

data class CatalogStatus(
    val storedPokemonCount: Int,
    val expectedPokemonCount: Int?,
    val lastSuccessfulSyncEpochMillis: Long?,
    val syncState: CatalogSyncState,
) {
    val hasUsableData: Boolean = storedPokemonCount > 0
    val isComplete: Boolean =
        expectedPokemonCount != null && expectedPokemonCount > 0 && storedPokemonCount >= expectedPokemonCount
}

sealed interface CatalogSyncState {
    data object Idle : CatalogSyncState
    data object Syncing : CatalogSyncState
    data class Failed(
        val reason: SyncFailure,
    ) : CatalogSyncState
}

enum class SyncFailure {
    NETWORK,
    SERVER,
    INCOMPLETE_RESPONSE,
    STORAGE,
    UNKNOWN,
}

sealed interface CatalogSyncResult {
    data class Success(
        val synchronizedPokemonCount: Int,
    ) : CatalogSyncResult

    data class Failure(
        val reason: SyncFailure,
        val hasUsableLocalData: Boolean,
    ) : CatalogSyncResult
}
