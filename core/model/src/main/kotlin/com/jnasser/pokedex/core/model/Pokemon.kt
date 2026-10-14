package com.jnasser.pokedex.core.model

data class PokemonSummary(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val types: List<PokemonType>,
    val isFavorite: Boolean,
)

data class PokemonDetail(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val types: List<PokemonType>,
    val heightDecimetres: Int,
    val weightHectograms: Int,
    val abilities: List<PokemonAbility>,
    val stats: List<PokemonStat>,
    val isFavorite: Boolean,
)

data class PokemonType(
    val slot: Int,
    val name: String,
)

data class PokemonAbility(
    val slot: Int,
    val name: String,
    val isHidden: Boolean,
)

data class PokemonStat(
    val name: String,
    val baseValue: Int,
)
