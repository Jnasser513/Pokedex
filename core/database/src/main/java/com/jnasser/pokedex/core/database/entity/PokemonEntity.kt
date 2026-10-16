package com.jnasser.pokedex.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon")
data class PokemonEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String?,
    @ColumnInfo(name = "height_decimetres")
    val heightDecimetres: Int?,
    @ColumnInfo(name = "weight_hectograms")
    val weightHectograms: Int?,
    @ColumnInfo(name = "details_complete")
    val detailsComplete: Boolean,
)
