package com.nasser.pokedexlsi.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.nasser.pokedexlsi.PokedexApplication
import com.nasser.pokedexlsi.databinding.AboutPokemonFragmentBinding
import com.nasser.pokedexlsi.ui.viewmodel.PokedexViewModel
import com.nasser.pokedexlsi.ui.viewmodel.PokedexViewModelFactory

class AboutPokemonFragment: Fragment() {

    private lateinit var mBinding: AboutPokemonFragmentBinding

    private val pokemonFactory: PokedexViewModelFactory by lazy {
        val app = requireActivity().application as PokedexApplication
        PokedexViewModelFactory(app.pokemonRepository)
    }

    private val pokedexViewModel: PokedexViewModel by viewModels {
        pokemonFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mBinding = AboutPokemonFragmentBinding.inflate(inflater, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.apply {
            viewmodel = pokedexViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        setUpListeners()
    }

    private fun setUpListeners() {
        
    }

}