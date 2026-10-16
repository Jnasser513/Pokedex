package com.jnasser.pokedex.feature.pokemondetail

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import app.cash.turbine.test
import com.jnasser.pokedex.core.domain.repository.PokemonRepository
import com.jnasser.pokedex.core.domain.usecase.ObservePokemonDetailUseCase
import com.jnasser.pokedex.core.domain.usecase.SetFavoriteUseCase
import com.jnasser.pokedex.core.model.CatalogStatus
import com.jnasser.pokedex.core.model.CatalogSyncResult
import com.jnasser.pokedex.core.model.PokemonAbility
import com.jnasser.pokedex.core.model.PokemonDetail
import com.jnasser.pokedex.core.model.PokemonStat
import com.jnasser.pokedex.core.model.PokemonSummary
import com.jnasser.pokedex.core.model.PokemonType
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakePokemonRepository()

    @Test
    fun `valid id starts by loading its local detail`() = runTest {
        repository.detailSource = emptyFlow()

        val viewModel = createViewModel(pokemonId = 6)

        assertEquals(PokemonDetailUiState.Loading, viewModel.uiState.value)
        assertEquals(listOf(6), repository.observedIds)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `local detail becomes content`() = runTest {
        val pokemon = charizard()
        repository.detailSource = flowOf(pokemon)

        val viewModel = createViewModel(pokemonId = pokemon.id)
        advanceUntilIdle()

        assertEquals(PokemonDetailUiState.Content(pokemon), viewModel.uiState.value)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `missing or incomplete local detail becomes not found`() = runTest {
        repository.detailSource = flowOf(null)

        val viewModel = createViewModel(pokemonId = 999)
        advanceUntilIdle()

        assertEquals(PokemonDetailUiState.NotFound, viewModel.uiState.value)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `non positive id is rejected without observing repository`() = runTest {
        val viewModel = createViewModel(pokemonId = 0)

        assertEquals(PokemonDetailUiState.InvalidPokemonId, viewModel.uiState.value)
        assertTrue(repository.observedIds.isEmpty())
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `local read failure exposes retryable error and retry resubscribes`() = runTest {
        repository.detailSource = flow { throw IOException("database unavailable") }
        val viewModel = createViewModel(pokemonId = 6)
        advanceUntilIdle()
        assertEquals(PokemonDetailUiState.Error, viewModel.uiState.value)

        val pokemon = charizard()
        repository.detailSource = flowOf(pokemon)
        viewModel.onAction(PokemonDetailAction.Retry)
        advanceUntilIdle()

        assertEquals(PokemonDetailUiState.Content(pokemon), viewModel.uiState.value)
        assertEquals(listOf(6, 6), repository.observedIds)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `favorite intent delegates the selected value`() = runTest {
        repository.detailSource = flowOf(charizard())
        val viewModel = createViewModel(pokemonId = 6)

        viewModel.onAction(PokemonDetailAction.FavoriteChanged(isFavorite = true))
        advanceUntilIdle()

        assertEquals(6 to true, repository.lastFavoriteUpdate)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `favorite failure emits one transient effect`() = runTest {
        repository.detailSource = flowOf(charizard())
        repository.favoriteFailure = IOException("database unavailable")
        val viewModel = createViewModel(pokemonId = 6)

        viewModel.effects.test {
            viewModel.onAction(PokemonDetailAction.FavoriteChanged(isFavorite = true))

            assertEquals(PokemonDetailEffect.FavoriteUpdateFailed, awaitItem())
            expectNoEvents()
        }
        viewModel.viewModelScope.cancel()
    }

    private fun createViewModel(pokemonId: Int) = PokemonDetailViewModel(
        pokemonId = pokemonId,
        observePokemonDetail = ObservePokemonDetailUseCase(repository),
        setFavorite = SetFavoriteUseCase(repository),
    )
}

private class FakePokemonRepository : PokemonRepository {
    var detailSource: Flow<PokemonDetail?> = MutableStateFlow(null)
    val observedIds = mutableListOf<Int>()
    var lastFavoriteUpdate: Pair<Int, Boolean>? = null
    var favoriteFailure: Throwable? = null

    override fun getPagedPokemon(query: String): Flow<PagingData<PokemonSummary>> = emptyFlow()

    override fun observePokemon(id: Int): Flow<PokemonDetail?> {
        observedIds += id
        return detailSource
    }

    override fun observeFavorites(): Flow<List<PokemonSummary>> = emptyFlow()

    override fun observeCatalogStatus(): Flow<CatalogStatus> = emptyFlow()

    override suspend fun setFavorite(id: Int, isFavorite: Boolean) {
        favoriteFailure?.let { throw it }
        lastFavoriteUpdate = id to isFavorite
    }

    override suspend fun synchronizeCatalog(): CatalogSyncResult =
        CatalogSyncResult.Success(synchronizedPokemonCount = 0)
}

private fun charizard() = PokemonDetail(
    id = 6,
    name = "charizard",
    imageUrl = "https://images.test/6.png",
    types = listOf(
        PokemonType(slot = 1, name = "fire"),
        PokemonType(slot = 2, name = "flying"),
    ),
    heightDecimetres = 17,
    weightHectograms = 905,
    abilities = listOf(
        PokemonAbility(slot = 1, name = "blaze", isHidden = false),
    ),
    stats = listOf(
        PokemonStat(name = "hp", baseValue = 78),
        PokemonStat(name = "attack", baseValue = 84),
    ),
    isFavorite = false,
)
