package com.jnasser.pokedex.core.database.dao

import org.junit.Assert.assertEquals
import org.junit.Test

class PokemonSearchQueryTest {
    @Test
    fun `blank query returns all pokemon`() {
        assertEquals(PokemonSearchQuery.All, PokemonSearchQuery.from("   "))
    }

    @Test
    fun `name query is trimmed and normalized`() {
        assertEquals(
            PokemonSearchQuery.Name("charizard"),
            PokemonSearchQuery.from("  ChArIzArD  "),
        )
    }

    @Test
    fun `number query accepts plain and formatted values`() {
        assertEquals(PokemonSearchQuery.Number(25), PokemonSearchQuery.from("25"))
        assertEquals(PokemonSearchQuery.Number(9), PokemonSearchQuery.from("#0009"))
    }

    @Test
    fun `non numeric formatted query is treated as a name`() {
        assertEquals(
            PokemonSearchQuery.Name("#pikachu"),
            PokemonSearchQuery.from("#Pikachu"),
        )
    }
}
