package com.jnasser.pokedex.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun PokedexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalPokedexSpacing provides PokedexSpacing(),
        LocalPokedexElevations provides PokedexElevations(),
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) PokedexDarkColorScheme else PokedexLightColorScheme,
            typography = PokedexTypography,
            shapes = PokedexShapes,
            content = content,
        )
    }
}
