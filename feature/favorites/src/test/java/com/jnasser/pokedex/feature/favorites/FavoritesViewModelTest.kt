package com.jnasser.pokedex.feature.favorites

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import app.cash.turbine.test
import com.jnasser.pokedex.core.domain.repository.PokemonRepository
import com.jnasser.pokedex.core.domain.usecase.ObserveFavoritesUseCase
import com.jnasser.pokedex.core.domain.usecase.SetFavoriteUseCase
import com.jnasser.pokedex.core.model.CatalogStatus
import com.jnasser.pokedex.core.model.CatalogSyncResult
import com.jnasser.pokedex.core.model.PokemonDetail
import com.jnasser.pokedex.core.model.PokemonSummary
import com.jnasser.pokedex.core.model.PokemonType
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakePokemonRepository()

    @Test
    fun `screen remains loading until local source emits`() = runTest {
        repository.favoritesFlow = emptyFlow()

        val viewModel = createViewModel()

        assertEquals(FavoritesUiState.Loading, viewModel.uiState.value)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `local favorites become content and react to changes`() = runTest {
        val charizard = pokemonSummary(id = 6, name = "charizard")
        repository.favoritesSource.value = listOf(charizard)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(FavoritesUiState.Content(listOf(charizard)), viewModel.uiState.value)

        val blastoise = pokemonSummary(id = 9, name = "blastoise")
        repository.favoritesSource.value = listOf(charizard, blastoise)
        advanceUntilIdle()

        assertEquals(
            FavoritesUiState.Content(listOf(charizard, blastoise)),
            viewModel.uiState.value,
        )
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `empty local favorites expose empty state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(FavoritesUiState.Empty, viewModel.uiState.value)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `local read failure exposes error and retry resubscribes`() = runTest {
        repository.favoritesFlow = flow { throw IOException("database unavailable") }
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(FavoritesUiState.Error, viewModel.uiState.value)

        val favorite = pokemonSummary(id = 25, name = "pikachu")
        repository.favoritesFlow = MutableStateFlow(listOf(favorite))
        viewModel.onAction(FavoritesAction.Retry)
        advanceUntilIdle()

        assertEquals(FavoritesUiState.Content(listOf(favorite)), viewModel.uiState.value)
        assertEquals(2, repository.observationCount)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `remove intent delegates false for selected pokemon`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(FavoritesAction.RemoveFavorite(pokemonId = 6))
        advanceUntilIdle()

        assertEquals(6 to false, repository.lastFavoriteUpdate)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `remove failure emits one transient effect`() = runTest {
        repository.favoriteFailure = IOException("database unavailable")
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onAction(FavoritesAction.RemoveFavorite(pokemonId = 6))

            assertEquals(FavoritesEffect.FavoriteUpdateFailed, awaitItem())
            expectNoEvents()
        }
        viewModel.viewModelScope.cancel()
    }

    private fun createViewModel() = FavoritesViewModel(
        observeFavorites = ObserveFavoritesUseCase(repository),
        setFavorite = SetFavoriteUseCase(repository),
    )
}

private class FakePokemonRepository : PokemonRepository {
    val favoritesSource = MutableStateFlow<List<PokemonSummary>>(emptyList())
    var favoritesFlow: Flow<List<PokemonSummary>> = favoritesSource
    var observationCount = 0
    var lastFavoriteUpdate: Pair<Int, Boolean>? = null
    var favoriteFailure: Throwable? = null

    override fun getPagedPokemon(query: String): Flow<PagingData<PokemonSummary>> = emptyFlow()

    override fun observePokemon(id: Int): Flow<PokemonDetail?> = emptyFlow()

    override fun observeFavorites(): Flow<List<PokemonSummary>> {
        observationCount += 1
        return favoritesFlow
    }

    override fun observeCatalogStatus(): Flow<CatalogStatus> = emptyFlow()

    override suspend fun setFavorite(id: Int, isFavorite: Boolean) {
        favoriteFailure?.let { throw it }
        lastFavoriteUpdate = id to isFavorite
    }

    override suspend fun synchronizeCatalog(): CatalogSyncResult =
        CatalogSyncResult.Success(synchronizedPokemonCount = 0)
}

private fun pokemonSummary(
    id: Int,
    name: String,
) = PokemonSummary(
    id = id,
    name = name,
    imageUrl = "https://images.test/$id.png",
    types = listOf(PokemonType(slot = 1, name = "fire")),
    isFavorite = true,
)
