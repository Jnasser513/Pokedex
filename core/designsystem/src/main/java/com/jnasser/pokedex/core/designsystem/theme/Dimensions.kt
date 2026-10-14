package com.jnasser.pokedex.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class PokedexSpacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val compactScreenMargin: Dp = 16.dp,
    val mediumScreenMargin: Dp = 24.dp,
    val expandedScreenMargin: Dp = 32.dp,
    val minimumTouchTarget: Dp = 48.dp,
)

@Immutable
data class PokedexElevations(
    val level0: Dp = 0.dp,
    val level1: Dp = 1.dp,
    val level2: Dp = 3.dp,
    val level3: Dp = 6.dp,
    val level4: Dp = 8.dp,
)

internal val LocalPokedexSpacing = staticCompositionLocalOf { PokedexSpacing() }
internal val LocalPokedexElevations = staticCompositionLocalOf { PokedexElevations() }

object PokedexDesignSystem {
    val spacing: PokedexSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalPokedexSpacing.current

    val elevations: PokedexElevations
        @Composable
        @ReadOnlyComposable
        get() = LocalPokedexElevations.current
}
