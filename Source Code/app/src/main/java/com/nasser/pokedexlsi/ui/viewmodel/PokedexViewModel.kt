package com.nasser.pokedexlsi.ui.viewmodel

import android.util.Log
import android.view.View
import androidx.lifecycle.*
import com.nasser.pokedexlsi.R
import com.nasser.pokedexlsi.data.entity.Pokemon
import com.nasser.pokedexlsi.repository.PokemonRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.lang.Exception

class PokedexViewModel(private val repository: PokemonRepository): ViewModel() {
    var key = MutableLiveData("")
    var pokemon = MutableLiveData<Pokemon>()
    private var _loading = MutableLiveData(View.GONE)
    val loading: LiveData<Int> get() = _loading
    private var _error = MutableLiveData<Int?>(null)
    val error: LiveData<Int?> get() = _error
    val pokemons = repository.findAll()

    /**
     * Busca un pokemon
     */
    fun search() {
        _error.value = null
        viewModelScope.launch {
            try {
                val key = key.value
                if (key.isNullOrEmpty()) {
                    _error.value = R.string.error_pokemon_empty
                } else {
                    pokemon.value = repository.search(key)
                }

            } catch (e: HttpException) {
                _error.value = R.string.error_not_found
            }
        }
    }
}