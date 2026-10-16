package com.jnasser.pokedex.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorContrastTest {
    @Test
    fun `light theme uses structured Stitch tokens`() {
        assertEquals(Color(0xFF930009), PokedexLightColorScheme.primary)
        assertEquals(Color(0xFFBA1A1A), PokedexLightColorScheme.primaryContainer)
        assertEquals(Color(0xFFCDE7ED), PokedexLightColorScheme.secondaryContainer)
        assertEquals(Color(0xFFFEF7FF), PokedexLightColorScheme.surface)
        assertEquals(Color(0xFFF9F1FD), PokedexLightColorScheme.surfaceContainerLow)
    }

    @Test
    fun `light theme content colors meet normal text contrast`() {
        assertThemeContrast(PokedexLightColorScheme)
    }

    @Test
    fun `dark theme content colors meet normal text contrast`() {
        assertThemeContrast(PokedexDarkColorScheme)
    }

    @Test
    fun `pokemon type tokens meet normal text contrast`() {
        val tokens = listOf(
            PokemonTypeTokens.Fire,
            PokemonTypeTokens.Water,
            PokemonTypeTokens.Grass,
            PokemonTypeTokens.Electric,
            PokemonTypeTokens.Psychic,
            PokemonTypeTokens.Ghost,
            PokemonTypeTokens.Dragon,
        )

        tokens.forEach { colors ->
            assertContrast(
                foreground = colors.content,
                background = colors.container,
            )
        }
    }

    private fun assertThemeContrast(colors: ColorScheme) {
        listOf(
            colors.onPrimary to colors.primary,
            colors.onPrimaryContainer to colors.primaryContainer,
            colors.onSecondary to colors.secondary,
            colors.onSecondaryContainer to colors.secondaryContainer,
            colors.onTertiary to colors.tertiary,
            colors.onTertiaryContainer to colors.tertiaryContainer,
            colors.onError to colors.error,
            colors.onErrorContainer to colors.errorContainer,
            colors.onSurface to colors.surface,
            colors.onSurfaceVariant to colors.surfaceVariant,
        ).forEach { (foreground, background) ->
            assertContrast(foreground = foreground, background = background)
        }
    }

    private fun assertContrast(
        foreground: Color,
        background: Color,
    ) {
        val ratio = contrastRatio(foreground = foreground, background = background)
        assertTrue("Expected contrast >= 4.5, but was $ratio", ratio >= 4.5)
    }

    private fun contrastRatio(
        foreground: Color,
        background: Color,
    ): Double {
        val foregroundLuminance = foreground.relativeLuminance()
        val backgroundLuminance = background.relativeLuminance()
        val lighter = max(foregroundLuminance, backgroundLuminance)
        val darker = min(foregroundLuminance, backgroundLuminance)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun Color.relativeLuminance(): Double =
        0.2126 * red.toLinearChannel() +
            0.7152 * green.toLinearChannel() +
            0.0722 * blue.toLinearChannel()

    private fun Float.toLinearChannel(): Double {
        val channel = toDouble()
        return if (channel <= 0.04045) {
            channel / 12.92
        } else {
            ((channel + 0.055) / 1.055).pow(2.4)
        }
    }
}
