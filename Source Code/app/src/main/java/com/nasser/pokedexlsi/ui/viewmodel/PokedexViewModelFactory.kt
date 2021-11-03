package com.nasser.pokedexlsi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nasser.pokedexlsi.repository.PokemonRepository

class PokedexViewModelFactory(private val repository: PokemonRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(PokedexViewModel::class.java)) {
            return PokedexViewModel(repository) as T
        }
        throw Exception("Unknown type of viewmodel")
    }
}