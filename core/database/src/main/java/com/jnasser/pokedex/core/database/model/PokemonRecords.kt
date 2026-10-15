package com.jnasser.pokedex.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import com.jnasser.pokedex.core.database.entity.PokemonAbilityEntity
import com.jnasser.pokedex.core.database.entity.PokemonEntity
import com.jnasser.pokedex.core.database.entity.PokemonStatEntity
import com.jnasser.pokedex.core.database.entity.PokemonTypeEntity

data class PokemonSummaryRecord(
    @Embedded
    val pokemon: PokemonEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "pokemon_id",
    )
    val types: List<PokemonTypeEntity>,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,
)

data class PokemonDetailRecord(
    @Embedded
    val pokemon: PokemonEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "pokemon_id",
    )
    val types: List<PokemonTypeEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "pokemon_id",
    )
    val abilities: List<PokemonAbilityEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "pokemon_id",
    )
    val stats: List<PokemonStatEntity>,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,
)
