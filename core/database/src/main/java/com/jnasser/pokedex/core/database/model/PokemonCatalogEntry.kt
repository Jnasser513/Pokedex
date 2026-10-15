package com.jnasser.pokedex.core.database.model

import com.jnasser.pokedex.core.database.entity.PokemonAbilityEntity
import com.jnasser.pokedex.core.database.entity.PokemonEntity
import com.jnasser.pokedex.core.database.entity.PokemonStatEntity
import com.jnasser.pokedex.core.database.entity.PokemonTypeEntity

data class PokemonCatalogEntry(
    val pokemon: PokemonEntity,
    val types: List<PokemonTypeEntity>,
    val abilities: List<PokemonAbilityEntity>,
    val stats: List<PokemonStatEntity>,
) {
    init {
        val pokemonId = pokemon.id
        require(types.all { it.pokemonId == pokemonId })
        require(abilities.all { it.pokemonId == pokemonId })
        require(stats.all { it.pokemonId == pokemonId })
    }
}
