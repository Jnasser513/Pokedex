package com.nasser.pokedexlsi.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nasser.pokedexlsi.R
import com.nasser.pokedexlsi.data.network.PokeResult
import kotlinx.android.synthetic.main.pokemon_item.view.*

class PokeListAdapter(val pokemonClick: (Int) -> Unit): RecyclerView.Adapter<PokeListAdapter.SearchViewHolder>() {


    var pokemonList: List<PokeResult> = emptyList<PokeResult>()

    fun setData(list: List<PokeResult>){
        pokemonList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.pokemon_item, parent,false))
    }

    override fun getItemCount(): Int {
        return pokemonList.size
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val pokemon = pokemonList[position]
        holder.itemView.name_pokemon.text = "${pokemon.name}"

        holder.itemView.setOnClickListener { pokemonClick(position + 1) }
    }

    class SearchViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    /*
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

    override fun getItemCount(): Int = pokemons?.size ?: 0*/
}