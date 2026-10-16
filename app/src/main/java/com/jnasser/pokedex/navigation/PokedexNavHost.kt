package com.jnasser.pokedex.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jnasser.pokedex.feature.pokemondetail.POKEMON_ID_ARGUMENT
import com.jnasser.pokedex.feature.pokemondetail.ui.PokemonDetailRoute
import com.jnasser.pokedex.feature.pokemonlist.ui.PokemonListRoute

private const val POKEMON_LIST_ROUTE = "pokemon"
private const val POKEMON_DETAIL_ROUTE = "pokemon/{$POKEMON_ID_ARGUMENT}"

@Composable
fun PokedexNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = POKEMON_LIST_ROUTE,
        modifier = modifier,
    ) {
        composable(POKEMON_LIST_ROUTE) {
            PokemonListRoute(
                onPokemonClick = { pokemonId ->
                    navController.navigate("pokemon/$pokemonId")
                },
            )
        }
        composable(
            route = POKEMON_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(POKEMON_ID_ARGUMENT) {
                    type = NavType.IntType
                }
            ),
        ) {
            PokemonDetailRoute(onBackClick = navController::navigateUp)
        }
    }
}
