package com.nasser.pokedexlsi.ui.viewmodel

import android.view.View
import androidx.lifecycle.*
import com.nasser.pokedexlsi.R
import com.nasser.pokedexlsi.data.entity.Pokemon
import com.nasser.pokedexlsi.data.entity.Pokemon1
import com.nasser.pokedexlsi.data.network.PokeAPI.service
import com.nasser.pokedexlsi.data.network.PokeApiResponse
import com.nasser.pokedexlsi.data.network.PokeResult
import com.nasser.pokedexlsi.repository.PokemonRepository
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class PokedexViewModel(private val repository: PokemonRepository): ViewModel() {
    var key = MutableLiveData("")
    var pokemon = MutableLiveData<Pokemon>()
    private var _loading = MutableLiveData(View.GONE)
    val loading: LiveData<Int> get() = _loading
    private var _error = MutableLiveData<Int?>(null)
    val error: LiveData<Int?> get() = _error
    val pokemons = repository.findAll()

    val pokemonList = MutableLiveData<List<PokeResult>>()
    val pokemonDetails = MutableLiveData<Pokemon1>()

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

    fun getPokemonList(){
        val call = service.getPokemonList(150,0)

        call.enqueue(object : Callback<PokeApiResponse> {
            override fun onResponse(call: Call<PokeApiResponse>, response: Response<PokeApiResponse>) {
                response.body()?.results?.let { list ->
                    pokemonList.postValue(list)
                }

            }

            override fun onFailure(call: Call<PokeApiResponse>, t: Throwable) {
                call.cancel()
            }

        })
    }

    fun getPokemonDetails(id: Int) {
        val call = service.getPokemonDetails(id)

        call.enqueue(object : Callback<Pokemon1> {
            override fun onResponse(call: Call<Pokemon1>, response: Response<Pokemon1>) {
                response.body()?.let { pokemon ->
                    pokemonDetails.postValue(pokemon)
                }
            }

            override fun onFailure(call: Call<Pokemon1>, t: Throwable) {
                call.cancel()
            }
        })
    }
}