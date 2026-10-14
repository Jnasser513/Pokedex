package com.jnasser.pokedex.core.domain.usecase

import androidx.paging.PagingData
import com.jnasser.pokedex.core.domain.repository.PokemonRepository
import com.jnasser.pokedex.core.model.CatalogStatus
import com.jnasser.pokedex.core.model.CatalogSyncResult
import com.jnasser.pokedex.core.model.PokemonDetail
import com.jnasser.pokedex.core.model.PokemonSummary
import com.jnasser.pokedex.core.model.SyncFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PokemonUseCasesTest {
    private val repository = RecordingPokemonRepository()

    @Test
    fun `paged pokemon normalizes whitespace and case`() {
        GetPagedPokemonUseCase(repository)("  PiKaChu  ")

        assertEquals("pikachu", repository.lastQuery)
    }

    @Test
    fun `detail rejects a non-positive id`() {
        assertThrows(IllegalArgumentException::class.java) {
            ObservePokemonDetailUseCase(repository)(0)
        }
    }

    @Test
    fun `favorite update reaches repository`() = runTest {
        SetFavoriteUseCase(repository)(id = 25, isFavorite = true)

        assertEquals(25 to true, repository.lastFavoriteUpdate)
    }
}

private class RecordingPokemonRepository : PokemonRepository {
    var lastQuery: String? = null
    var lastFavoriteUpdate: Pair<Int, Boolean>? = null

    override fun getPagedPokemon(query: String): Flow<PagingData<PokemonSummary>> {
        lastQuery = query
        return flowOf(PagingData.empty())
    }

    override fun observePokemon(id: Int): Flow<PokemonDetail?> = flowOf(null)

    override fun observeFavorites(): Flow<List<PokemonSummary>> = flowOf(emptyList())

    override fun observeCatalogStatus(): Flow<CatalogStatus> = emptyFlow()

    override suspend fun setFavorite(id: Int, isFavorite: Boolean) {
        lastFavoriteUpdate = id to isFavorite
    }

    override suspend fun synchronizeCatalog(): CatalogSyncResult =
        CatalogSyncResult.Failure(
            reason = SyncFailure.UNKNOWN,
            hasUsableLocalData = false,
        )
}
