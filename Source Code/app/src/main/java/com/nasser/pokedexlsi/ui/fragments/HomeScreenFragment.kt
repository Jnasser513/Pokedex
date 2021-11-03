package com.nasser.pokedexlsi.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nasser.pokedexlsi.PokedexApplication
import com.nasser.pokedexlsi.R
import com.nasser.pokedexlsi.databinding.HomeScreenFragmentBinding
import com.nasser.pokedexlsi.repository.PokemonRepository
import com.nasser.pokedexlsi.ui.adapters.PokeListAdapter
import com.nasser.pokedexlsi.ui.viewmodel.PokedexViewModel
import com.nasser.pokedexlsi.ui.viewmodel.PokedexViewModelFactory

class HomeScreenFragment: Fragment() {

    private var mBinding: HomeScreenFragmentBinding? = null
    private val binding get() = mBinding!!

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewmodel = pokedexViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.pokedexSearchInputEdit.doAfterTextChanged { message ->
            pokedexViewModel.key.value = message.toString()
        }

        pokedexViewModel.pokemon.observe(viewLifecycleOwner) { pokemon ->
            val display = binding.pokedexSearchResult
            if (pokemon.name.isNotEmpty()) {
                display.text =
                    getString(R.string.pokemon_info, pokemon.id, pokemon.name)
            } else {
                display.text = getString(R.string.hint_pokemon)
            }
        }

        pokedexViewModel.error.observe(viewLifecycleOwner) { error ->
            if (error == null) {
                binding.pokedexSearchInputLayout.error = null
            } else {
                binding.pokedexSearchInputLayout.error = getString(error)
            }

        }

        val rvAdpater = PokeListAdapter()

        val rv = binding.pokedexRecyclerview.apply {
            adapter = rvAdpater
            layoutManager = LinearLayoutManager(requireContext())
        }

        pokedexViewModel.pokemons.observe(viewLifecycleOwner) {
            rvAdpater.setData(it)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment PokedexFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeScreenFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}