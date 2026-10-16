package com.jnasser.pokedex.core.designsystem.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PokemonTypeTokensTest {
    @Test
    fun `colorsFor normalizes remote type names`() {
        assertEquals(PokemonTypeTokens.Fire, PokemonTypeTokens.colorsFor(" FIRE "))
        assertEquals(PokemonTypeTokens.Electric, PokemonTypeTokens.colorsFor("Electric"))
    }

    @Test
    fun `colorsFor returns null when design has no approved type token`() {
        assertNull(PokemonTypeTokens.colorsFor("flying"))
        assertNull(PokemonTypeTokens.colorsFor("unknown"))
    }
}
