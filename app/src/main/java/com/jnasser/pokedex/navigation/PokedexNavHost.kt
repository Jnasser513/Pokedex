package com.jnasser.pokedex.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.jnasser.pokedex.R
import com.jnasser.pokedex.feature.favorites.ui.FavoritesRoute
import com.jnasser.pokedex.feature.pokemondetail.ui.PokemonDetailRoute
import com.jnasser.pokedex.feature.pokemonlist.ui.PokemonListRoute
import kotlinx.serialization.Serializable

@Serializable
private data object PokemonListKey : NavKey

@Serializable
private data object FavoritesKey : NavKey

@Serializable
private data class PokemonDetailKey(
    val pokemonId: Int,
) : NavKey

@Composable
fun PokedexNavHost(
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(PokemonListKey)
    val navigateBack = {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }
    val navigateToPokemonList = {
        while (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }
    val navigateToFavorites: () -> Unit = {
        navigateToPokemonList()
        backStack.add(FavoritesKey)
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = navigateBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<PokemonListKey> {
                MainDestinationScaffold(
                    selectedDestination = MainDestination.Pokedex,
                    onPokedexClick = navigateToPokemonList,
                    onFavoritesClick = navigateToFavorites,
                ) { contentPadding ->
                    PokemonListRoute(
                        onPokemonClick = { pokemonId ->
                            backStack.add(PokemonDetailKey(pokemonId))
                        },
                        modifier = Modifier.padding(contentPadding),
                    )
                }
            }
            entry<FavoritesKey> {
                MainDestinationScaffold(
                    selectedDestination = MainDestination.Favorites,
                    onPokedexClick = navigateToPokemonList,
                    onFavoritesClick = navigateToFavorites,
                ) { contentPadding ->
                    FavoritesRoute(
                        onPokemonClick = { pokemonId ->
                            backStack.add(PokemonDetailKey(pokemonId))
                        },
                        onBrowsePokedex = navigateToPokemonList,
                        modifier = Modifier.padding(contentPadding),
                    )
                }
            }
            entry<PokemonDetailKey> { key ->
                PokemonDetailRoute(
                    pokemonId = key.pokemonId,
                    onBackClick = navigateBack,
                )
            }
        },
    )
}

@Composable
private fun MainDestinationScaffold(
    selectedDestination: MainDestination,
    onPokedexClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedDestination == MainDestination.Pokedex,
                    onClick = onPokedexClick,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(R.string.navigation_pokedex)) },
                )
                NavigationBarItem(
                    selected = selectedDestination == MainDestination.Favorites,
                    onClick = onFavoritesClick,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(R.string.navigation_favorites)) },
                )
            }
        },
        content = content,
    )
}

private enum class MainDestination {
    Pokedex,
    Favorites,
}
