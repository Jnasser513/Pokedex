package com.nasser.pokedexlsi.ui.fragments

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.nasser.pokedexlsi.PokedexApplication
import com.nasser.pokedexlsi.databinding.PokemonDetailActivityBinding
import com.nasser.pokedexlsi.ui.viewmodel.PokedexViewModel
import com.nasser.pokedexlsi.ui.viewmodel.PokedexViewModelFactory

class PokemonDetailsActivity: AppCompatActivity() {

    private lateinit var mBinding: PokemonDetailActivityBinding

    private val pokemonFactory: PokedexViewModelFactory by lazy {
        val app = this.application as PokedexApplication
        PokedexViewModelFactory(app.pokemonRepository)
    }

    private val pokedexViewModel: PokedexViewModel by viewModels {
        pokemonFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = PokemonDetailActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.apply {
            viewmodel = pokedexViewModel
            lifecycleOwner = this@PokemonDetailsActivity
        }

        initUI()
    }

    private fun initUI() {
        val id = intent.extras?.get("id") as Int

        pokedexViewModel.getPokemonDetails(id)

        pokedexViewModel.pokemonDetails.observe(this, Observer { pokemon ->
            mBinding.pokemonTitle.text = pokemon.name
            mBinding.pokemonNumber.text = pokemon.id.toString()
        })
    }

}