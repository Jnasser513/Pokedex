package com.nasser.pokedexlsi.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nasser.pokedexlsi.R
import com.nasser.pokedexlsi.databinding.DetailsTabActivityBinding
import com.nasser.pokedexlsi.ui.adapters.DetailsViewPagerAdapter
import com.nasser.pokedexlsi.ui.fragments.AbilitiesPokemonFragment
import com.nasser.pokedexlsi.ui.fragments.AboutPokemonFragment
import com.nasser.pokedexlsi.ui.fragments.EvolutionPokemonFragment
import com.nasser.pokedexlsi.ui.fragments.StatsPokemonFragment

class DetailsTabActivity: AppCompatActivity() {

    private lateinit var mBinding: DetailsTabActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DetailsTabActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setUpTabs()
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

}