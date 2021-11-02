package com.nasser.pokedexlsi.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nasser.pokedexlsi.databinding.AboutPokemonFragmentBinding

class AboutPokemonFragment: Fragment() {

    private lateinit var mBinding: AboutPokemonFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mBinding = AboutPokemonFragmentBinding.inflate(inflater, container, false)

        return mBinding.root
    }

}