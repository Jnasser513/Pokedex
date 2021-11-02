package com.nasser.pokedexlsi.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nasser.pokedexlsi.databinding.StatsPokemonFragmentBinding

class StatsPokemonFragment: Fragment() {

    private lateinit var mBinding: StatsPokemonFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mBinding = StatsPokemonFragmentBinding.inflate(inflater, container, false)

        return mBinding.root
    }

}