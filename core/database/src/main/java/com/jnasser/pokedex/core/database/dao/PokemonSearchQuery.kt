package com.jnasser.pokedex.core.database.dao

import java.util.Locale

internal sealed interface PokemonSearchQuery {
    data object All : PokemonSearchQuery

    data class Name(val value: String) : PokemonSearchQuery

    data class Number(val value: Int) : PokemonSearchQuery

    companion object {
        fun from(rawQuery: String): PokemonSearchQuery {
            val normalizedQuery = rawQuery
                .trim()
                .lowercase(Locale.ROOT)

            if (normalizedQuery.isEmpty()) return All

            val number = normalizedQuery
                .removePrefix("#")
                .takeIf(String::isNotEmpty)
                ?.toIntOrNull()

            return number?.let(::Number) ?: Name(normalizedQuery)
        }
    }
}
