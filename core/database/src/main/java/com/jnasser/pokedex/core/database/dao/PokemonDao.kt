package com.jnasser.pokedex.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.jnasser.pokedex.core.database.entity.PokemonAbilityEntity
import com.jnasser.pokedex.core.database.entity.PokemonEntity
import com.jnasser.pokedex.core.database.entity.PokemonStatEntity
import com.jnasser.pokedex.core.database.entity.PokemonTypeEntity
import com.jnasser.pokedex.core.database.model.PokemonCatalogEntry
import com.jnasser.pokedex.core.database.model.PokemonDetailRecord
import com.jnasser.pokedex.core.database.model.PokemonSummaryRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {
    fun pagingSource(query: String): PagingSource<Int, PokemonSummaryRecord> =
        when (val searchQuery = PokemonSearchQuery.from(query)) {
            PokemonSearchQuery.All -> pagingSourceAll()
            is PokemonSearchQuery.Name -> pagingSourceByName(searchQuery.value)
            is PokemonSearchQuery.Number -> pagingSourceByNumber(searchQuery.value)
        }

    @Transaction
    @Query(
        """
        SELECT pokemon.*, favorite.pokemon_id IS NOT NULL AS is_favorite
        FROM pokemon
        LEFT JOIN favorite ON favorite.pokemon_id = pokemon.id
        ORDER BY pokemon.id ASC
        """
    )
    fun pagingSourceAll(): PagingSource<Int, PokemonSummaryRecord>

    @Transaction
    @Query(
        """
        SELECT pokemon.*, favorite.pokemon_id IS NOT NULL AS is_favorite
        FROM pokemon
        LEFT JOIN favorite ON favorite.pokemon_id = pokemon.id
        WHERE INSTR(LOWER(pokemon.name), :nameQuery) > 0
        ORDER BY pokemon.id ASC
        """
    )
    fun pagingSourceByName(nameQuery: String): PagingSource<Int, PokemonSummaryRecord>

    @Transaction
    @Query(
        """
        SELECT pokemon.*, favorite.pokemon_id IS NOT NULL AS is_favorite
        FROM pokemon
        LEFT JOIN favorite ON favorite.pokemon_id = pokemon.id
        WHERE pokemon.id = :pokemonNumber
        ORDER BY pokemon.id ASC
        """
    )
    fun pagingSourceByNumber(pokemonNumber: Int): PagingSource<Int, PokemonSummaryRecord>

    @Transaction
    @Query(
        """
        SELECT pokemon.*, favorite.pokemon_id IS NOT NULL AS is_favorite
        FROM pokemon
        LEFT JOIN favorite ON favorite.pokemon_id = pokemon.id
        WHERE pokemon.id = :id
        """
    )
    fun observePokemon(id: Int): Flow<PokemonDetailRecord?>

    @Transaction
    @Query(
        """
        SELECT pokemon.*, 1 AS is_favorite
        FROM pokemon
        INNER JOIN favorite ON favorite.pokemon_id = pokemon.id
        ORDER BY favorite.created_at_epoch_millis DESC, pokemon.id ASC
        """
    )
    fun observeFavorites(): Flow<List<PokemonSummaryRecord>>

    @Query("SELECT COUNT(*) FROM pokemon")
    fun observePokemonCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pokemon")
    suspend fun pokemonCount(): Int

    @Transaction
    suspend fun upsertCatalog(entries: List<PokemonCatalogEntry>) {
        entries.forEach { entry ->
            upsertPokemon(entry.pokemon)
            deleteTypes(entry.pokemon.id)
            deleteAbilities(entry.pokemon.id)
            deleteStats(entry.pokemon.id)
            upsertTypes(entry.types)
            upsertAbilities(entry.abilities)
            upsertStats(entry.stats)
        }
    }

    @Upsert
    suspend fun upsertPokemon(pokemon: PokemonEntity)

    @Upsert
    suspend fun upsertTypes(types: List<PokemonTypeEntity>)

    @Upsert
    suspend fun upsertAbilities(abilities: List<PokemonAbilityEntity>)

    @Upsert
    suspend fun upsertStats(stats: List<PokemonStatEntity>)

    @Query("DELETE FROM pokemon_type WHERE pokemon_id = :pokemonId")
    suspend fun deleteTypes(pokemonId: Int)

    @Query("DELETE FROM pokemon_ability WHERE pokemon_id = :pokemonId")
    suspend fun deleteAbilities(pokemonId: Int)

    @Query("DELETE FROM pokemon_stat WHERE pokemon_id = :pokemonId")
    suspend fun deleteStats(pokemonId: Int)
}
