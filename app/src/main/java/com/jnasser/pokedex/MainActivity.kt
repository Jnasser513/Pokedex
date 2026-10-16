package com.jnasser.pokedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jnasser.pokedex.core.designsystem.theme.PokedexTheme
import com.jnasser.pokedex.feature.pokemonlist.ui.PokemonListRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokedexTheme {
                PokemonListRoute()
            }
        }
    }
}
