package com.nasser.pokedexlsi.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.nasser.pokedexlsi.PokedexApplication
import com.nasser.pokedexlsi.R
import com.nasser.pokedexlsi.databinding.HomeScreenFragmentBinding
import com.nasser.pokedexlsi.ui.adapters.PokeListAdapter
import com.nasser.pokedexlsi.ui.viewmodel.PokedexViewModel
import com.nasser.pokedexlsi.ui.viewmodel.PokedexViewModelFactory
import java.util.Observer

class HomeScreenFragment: Fragment() {

    private lateinit var mBinding: HomeScreenFragmentBinding
    private lateinit var mContext: Context
    private lateinit var mGridLayout:GridLayoutManager

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
        mBinding = HomeScreenFragmentBinding.inflate(inflater, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.apply {
            viewmodel = pokedexViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        mBinding.pokedexSearchInputEdit.doAfterTextChanged { message ->
            pokedexViewModel.key.value = message.toString()
        }

        setUpListeners()
    }

    private fun setUpListeners() {
        setUpRecyclerview()
        displayResult()
    }

    private fun setUpRecyclerview() {
        mGridLayout = GridLayoutManager(requireContext(), resources.getInteger(R.integer.main_columns))

        mBinding.pokedexRecyclerview.apply {
            setHasFixedSize(true)
            layoutManager = mGridLayout
            adapter = PokeListAdapter {
                val intent = Intent(requireContext(), PokemonDetailsActivity::class.java)
                intent.putExtra("id", it)
                startActivity(intent)
            }
        }

        pokedexViewModel.getPokemonList()

        pokedexViewModel.pokemonList.observe(viewLifecycleOwner, androidx.lifecycle.Observer { list ->
            (mBinding.pokedexRecyclerview.adapter as PokeListAdapter).setData(list)
        })
    }

    private fun displayResult() {
        pokedexViewModel.pokemon.observe(viewLifecycleOwner) { pokemon ->
            val display = mBinding.pokedexSearchResult
            if (pokemon.name.isNotEmpty()) {
                display.text =
                    getString(R.string.pokemon_info, pokemon.id, pokemon.name)
            } else {
                display.text = getString(R.string.hint_pokemon)
            }
        }

        pokedexViewModel.error.observe(viewLifecycleOwner) { error ->
            if (error == null) {
                mBinding.pokedexSearchInputLayout.error = null
            } else {
                mBinding.pokedexSearchInputLayout.error = getString(error)
            }

        }
    }
}