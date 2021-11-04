package com.nasser.pokedexlsi.ui.fragments

import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.nasser.pokedexlsi.PokedexApplication
import com.nasser.pokedexlsi.R
import com.nasser.pokedexlsi.databinding.PokemonDetailActivityBinding
import com.nasser.pokedexlsi.ui.adapters.DetailsViewPagerAdapter
import com.nasser.pokedexlsi.ui.viewmodel.PokedexViewModel
import com.nasser.pokedexlsi.ui.viewmodel.PokedexViewModelFactory

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

    private fun setUpListeners(){
        initUI()
        setUpTabs()
    }

    private fun initUI() {
        val id = intent.extras?.get("id") as Int

        pokedexViewModel.getPokemonDetails(id)

        pokedexViewModel.pokemonDetails.observe(this, Observer { pokemon ->
            mBinding.pokemonTitle.text = pokemon.name
            mBinding.pokemonNumber.text = pokemon.id.toString()
            //IMPORTANTE
            Glide.with(this).load(pokemon.sprites.frontDefault).into(mBinding.pokemonImg)
        })
        mediaPlayerObserver(id)
    }

    private fun setUpTabs() {
        val mAdapter = DetailsViewPagerAdapter(supportFragmentManager)
        mAdapter.addFragment(AboutPokemonFragment())
        mAdapter.addFragment(AbilitiesPokemonFragment())
        mAdapter.addFragment(EvolutionPokemonFragment())
        mAdapter.addFragment(StatsPokemonFragment())
        mBinding.viewPager.adapter = mAdapter
        mBinding.pokemonDetailTabLayout.setupWithViewPager(mBinding.viewPager)

        mBinding.pokemonDetailTabLayout.getTabAt(0)!!.setText(R.string.about_title)
        mBinding.pokemonDetailTabLayout.getTabAt(1)!!.setText(R.string.abilities_title)
        mBinding.pokemonDetailTabLayout.getTabAt(2)!!.setText(R.string.evolution_title)
        mBinding.pokemonDetailTabLayout.getTabAt(3)!!.setText(R.string.stats_title)

    }

    private fun mediaPlayerObserver(id: Int) {
        pokedexViewModel.mediaPlayer.observe(this, Observer { audio ->
            mBinding.actionEmitSound.setOnClickListener {
                mediaPlayer = MediaPlayer.create(this, pokedexViewModel.soundList[id])
                mediaPlayer?.start()
            }
        })
    }

    /*private fun emitSound(id: Int) {
        mBinding.actionEmitSound.setOnClickListener {
            mediaPlayer = MediaPlayer.create(this, soundList[id])
            mediaPlayer?.start()
        }
    }*/

}