package com.nasser.pokedexlsi.ui

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.nasser.pokedexlsi.PokedexApplication
import com.nasser.pokedexlsi.R
import com.nasser.pokedexlsi.data.entity.PokemonAbilities
import com.nasser.pokedexlsi.data.entity.PokemonType
import com.nasser.pokedexlsi.databinding.PokemonDetailActivityBinding
import com.nasser.pokedexlsi.ui.adapters.DetailsViewPagerAdapter
import com.nasser.pokedexlsi.ui.fragments.AbilitiesPokemonFragment
import com.nasser.pokedexlsi.ui.fragments.AboutPokemonFragment
import com.nasser.pokedexlsi.ui.fragments.EvolutionPokemonFragment
import com.nasser.pokedexlsi.ui.fragments.StatsPokemonFragment
import com.nasser.pokedexlsi.ui.viewmodel.PokedexViewModel
import com.nasser.pokedexlsi.ui.viewmodel.PokedexViewModelFactory
import java.util.*

class PokemonDetailsActivity: AppCompatActivity() {

    private lateinit var mBinding: PokemonDetailActivityBinding
    private var mediaPlayer: MediaPlayer? = null

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
    }

    override fun onStart() {
        super.onStart()
        setUpListeners()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
        finish()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        finish()
    }

    private fun setUpListeners(){
        initUI()
        //setUpTabs()
    }

    private fun initUI() {
        val id = intent.extras?.get("id") as Int

        pokedexViewModel.getPokemonDetails(id)

        pokedexViewModel.pokemonDetails.observe(this, Observer { pokemon ->
            mBinding.pokemonTitle.text = pokemon.name
            mBinding.pokemonNumber.text = pokemon.id.toString()
            //IMPORTANTE
            Glide.with(this).load(pokemon.sprites.frontDefault).into(mBinding.pokemonImg)
            setTypes(pokemon.types)
            mBinding.pokemonHeight.text = pokemon.height.toString()
            mBinding.pokemonWeight.text = pokemon.weight.toString()
            setAbilities(pokemon.abilities)
        })
        mediaPlayerObserver(id-1)
    }

    private fun setTypes(types: List<PokemonType>){
        val chips = mBinding.pokemonTypes
        chips.removeAllViews()
        types.forEach{
            chips.addView(Chip(this).apply {
                text = it.type.name.capitalize(Locale.getDefault())
            })
        }
    }

    private fun setAbilities(types: List<PokemonAbilities>){
        val chips = mBinding.pokemonAbilities
        chips.removeAllViews()
        types.forEach{
            chips.addView(Chip(this).apply {
                text = it.ability.name.capitalize(Locale.getDefault())
            })
        }
    }

    private fun mediaPlayerObserver(id: Int) {
        pokedexViewModel.mediaPlayer.observe(this, Observer {
            it?.let {
                if(it){
                    mediaPlayer = MediaPlayer.create(this, pokedexViewModel.soundList[id])
                    mediaPlayer?.start()
                }
                pokedexViewModel.endEmitSound()
            }
        })
    }

}