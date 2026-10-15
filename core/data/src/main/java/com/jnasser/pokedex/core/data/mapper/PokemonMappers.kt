package com.jnasser.pokedex.core.data.mapper

import com.jnasser.pokedex.core.database.entity.PokemonAbilityEntity
import com.jnasser.pokedex.core.database.entity.PokemonEntity
import com.jnasser.pokedex.core.database.entity.PokemonStatEntity
import com.jnasser.pokedex.core.database.entity.PokemonTypeEntity
import com.jnasser.pokedex.core.database.model.PokemonCatalogEntry
import com.jnasser.pokedex.core.database.model.PokemonDetailRecord
import com.jnasser.pokedex.core.database.model.PokemonSummaryRecord
import com.jnasser.pokedex.core.model.PokemonAbility
import com.jnasser.pokedex.core.model.PokemonDetail
import com.jnasser.pokedex.core.model.PokemonStat
import com.jnasser.pokedex.core.model.PokemonSummary
import com.jnasser.pokedex.core.model.PokemonType
import com.jnasser.pokedex.core.network.model.RemotePokemon

internal fun RemotePokemon.toEntity(): PokemonCatalogEntry =
    PokemonCatalogEntry(
        pokemon = PokemonEntity(
            id = id,
            name = name,
            imageUrl = imageUrl,
            heightDecimetres = heightDecimetres,
            weightHectograms = weightHectograms,
            detailsComplete = true,
        ),
        types = types.map {
            PokemonTypeEntity(
                pokemonId = id,
                slot = it.slot,
                name = it.name,
            )
        },
        abilities = abilities.map {
            PokemonAbilityEntity(
                pokemonId = id,
                slot = it.slot,
                name = it.name,
                isHidden = it.isHidden,
            )
        },
        stats = stats.map {
            PokemonStatEntity(
                pokemonId = id,
                slot = it.slot,
                name = it.name,
                baseValue = it.baseValue,
            )
        },
    )

internal fun PokemonSummaryRecord.toDomain(): PokemonSummary =
    PokemonSummary(
        id = pokemon.id,
        name = pokemon.name,
        imageUrl = pokemon.imageUrl,
        types = types
            .sortedBy(PokemonTypeEntity::slot)
            .map { PokemonType(slot = it.slot, name = it.name) },
        isFavorite = isFavorite,
    )

internal fun PokemonDetailRecord.toDomainOrNull(): PokemonDetail? {
    val height = pokemon.heightDecimetres
    val weight = pokemon.weightHectograms
    if (!pokemon.detailsComplete || height == null || weight == null) return null

    return PokemonDetail(
        id = pokemon.id,
        name = pokemon.name,
        imageUrl = pokemon.imageUrl,
        types = types
            .sortedBy(PokemonTypeEntity::slot)
            .map { PokemonType(slot = it.slot, name = it.name) },
        heightDecimetres = height,
        weightHectograms = weight,
        abilities = abilities
            .sortedBy(PokemonAbilityEntity::slot)
            .map {
                PokemonAbility(
                    slot = it.slot,
                    name = it.name,
                    isHidden = it.isHidden,
                )
            },
        stats = stats
            .sortedBy(PokemonStatEntity::slot)
            .map { PokemonStat(name = it.name, baseValue = it.baseValue) },
        isFavorite = isFavorite,
    )
}
