package com.jnasser.pokedex.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import java.util.Locale

@Immutable
data class PokemonTypeColors(
    val container: Color,
    val content: Color,
)

object PokemonTypeTokens {
    val Fire = PokemonTypeColors(
        container = Color(0xFFFFDAD4),
        content = Color(0xFF3E0400),
    )
    val Water = PokemonTypeColors(
        container = Color(0xFFD4E3FF),
        content = Color(0xFF001C3A),
    )
    val Grass = PokemonTypeColors(
        container = Color(0xFFCEE9C8),
        content = Color(0xFF07210B),
    )
    val Electric = PokemonTypeColors(
        container = Color(0xFFFFE16E),
        content = Color(0xFF241A00),
    )
    val Psychic = PokemonTypeColors(
        container = Color(0xFFFFD8E9),
        content = Color(0xFF3B0727),
    )
    val Ghost = PokemonTypeColors(
        container = Color(0xFFE7DEFF),
        content = Color(0xFF1E0F45),
    )
    val Dragon = PokemonTypeColors(
        container = Color(0xFFE3DFFF),
        content = Color(0xFF130068),
    )

    fun colorsFor(typeName: String): PokemonTypeColors? = when (
        typeName.trim().lowercase(Locale.ROOT)
    ) {
        "fire" -> Fire
        "water" -> Water
        "grass" -> Grass
        "electric" -> Electric
        "psychic" -> Psychic
        "ghost" -> Ghost
        "dragon" -> Dragon
        else -> null
    }
}
