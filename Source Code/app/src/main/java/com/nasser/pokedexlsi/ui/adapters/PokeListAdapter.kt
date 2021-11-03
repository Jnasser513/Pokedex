package com.nasser.pokedexlsi.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.nasser.pokedexlsi.R
import com.nasser.pokedexlsi.data.entity.PokemonType
import com.nasser.pokedexlsi.data.entity.PokemonWithType
import java.util.*

class PokeListAdapter: RecyclerView.Adapter<PokeListAdapter.PokemonViewHolder>() {

    private var pokemons: List<PokemonWithType>? = null

    fun setData(data: List<PokemonWithType>) {
        this.pokemons = data
        notifyDataSetChanged()
    }

    class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(data: PokemonWithType) {
            val idTextView = itemView.findViewById<TextView>(R.id.idPokemon)
            val nameTextView = itemView.findViewById<TextView>(R.id.name_pokemon)
            val imageView = itemView.findViewById<ImageView>(R.id.image_pokemon)
            setTypes(data.types)
            idTextView.text = data.pokemon.id.toString()
            nameTextView.text = data.pokemon.name.capitalize(Locale.ROOT)
            Glide.with(itemView)
                .load(data.pokemon.sprites.frontDefault)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.ic_pokeball)
                .into(imageView)
        }

        private fun setTypes(types: List<PokemonType>){
            val chips = itemView.findViewById<ChipGroup>(R.id.pokemon_types)
            chips.removeAllViews()
            types.forEach{
                chips.addView(Chip(itemView.context).apply {
                    text = it.type.name.capitalize(Locale.getDefault())
                })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pokemon_item, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val data = pokemons ?: return
        val pokemon = data[position]
        holder.bind(pokemon)

    }

    override fun getItemCount(): Int = pokemons?.size ?: 0
}