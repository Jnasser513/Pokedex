package com.jnasser.pokedex.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonListResponseDto(
    val count: Int,
    val results: List<NamedApiResourceDto>,
)

@Serializable
data class NamedApiResourceDto(
    val name: String,
    val url: String,
)

@Serializable
data class PokemonDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    @SerialName("is_default")
    val isDefault: Boolean,
    val abilities: List<PokemonAbilityDto>,
    val sprites: PokemonSpritesDto,
    val stats: List<PokemonStatDto>,
    val types: List<PokemonTypeDto>,
)

@Serializable
data class PokemonAbilityDto(
    @SerialName("is_hidden")
    val isHidden: Boolean,
    val slot: Int,
    val ability: NamedApiResourceDto,
)

@Serializable
data class PokemonStatDto(
    @SerialName("base_stat")
    val baseStat: Int,
    val stat: NamedApiResourceDto,
)

@Serializable
data class PokemonTypeDto(
    val slot: Int,
    val type: NamedApiResourceDto,
)

@Serializable
data class PokemonSpritesDto(
    @SerialName("front_default")
    val frontDefault: String? = null,
    val other: OtherPokemonSpritesDto? = null,
)

@Serializable
data class OtherPokemonSpritesDto(
    @SerialName("official-artwork")
    val officialArtwork: PokemonArtworkDto? = null,
)

@Serializable
data class PokemonArtworkDto(
    @SerialName("front_default")
    val frontDefault: String? = null,
)
