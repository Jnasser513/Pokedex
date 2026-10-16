package com.jnasser.pokedex.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "pokemon_type",
    primaryKeys = ["pokemon_id", "slot"],
    foreignKeys = [
        ForeignKey(
            entity = PokemonEntity::class,
            parentColumns = ["id"],
            childColumns = ["pokemon_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("pokemon_id")],
)
data class PokemonTypeEntity(
    @ColumnInfo(name = "pokemon_id")
    val pokemonId: Int,
    val slot: Int,
    val name: String,
)
