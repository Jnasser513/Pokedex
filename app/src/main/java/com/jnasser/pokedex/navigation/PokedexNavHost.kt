package com.jnasser.pokedex.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.jnasser.pokedex.feature.pokemondetail.ui.PokemonDetailRoute
import com.jnasser.pokedex.feature.pokemonlist.ui.PokemonListRoute
import kotlinx.serialization.Serializable

@Serializable
private data object PokemonListKey : NavKey

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
                PokemonListRoute(
                    onPokemonClick = { pokemonId ->
                        backStack.add(PokemonDetailKey(pokemonId))
                    },
                )
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
