package com.jnasser.pokedex.core.network.model

data class RemotePokemonCatalog(
    val expectedPokemonCount: Int,
    val pokemon: List<RemotePokemon>,
)

data class RemotePokemon(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val heightDecimetres: Int,
    val weightHectograms: Int,
    val types: List<RemotePokemonType>,
    val abilities: List<RemotePokemonAbility>,
    val stats: List<RemotePokemonStat>,
)

data class RemotePokemonType(
    val slot: Int,
    val name: String,
)

data class RemotePokemonAbility(
    val slot: Int,
    val name: String,
    val isHidden: Boolean,
)

data class RemotePokemonStat(
    val slot: Int,
    val name: String,
    val baseValue: Int,
)
