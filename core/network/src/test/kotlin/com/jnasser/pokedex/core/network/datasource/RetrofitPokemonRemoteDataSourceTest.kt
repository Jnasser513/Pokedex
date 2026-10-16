package com.jnasser.pokedex.core.network.datasource

import com.jnasser.pokedex.core.network.PokeApiService
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class RetrofitPokemonRemoteDataSourceTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `catalog response is mapped using official artwork`() = runTest {
        server.dispatcher = pokemonDispatcher()
        val dataSource = createDataSource(catalogSize = 2)

        val catalog = dataSource.fetchCatalog()

        assertEquals(2, catalog.expectedPokemonCount)
        assertEquals(listOf(1, 2), catalog.pokemon.map { it.id })
        assertEquals("https://images.test/bulbasaur.png", catalog.pokemon.first().imageUrl)
        assertEquals("grass", catalog.pokemon.first().types.first().name)
        assertEquals("hp", catalog.pokemon.first().stats.first().name)
        assertEquals(1, catalog.pokemon.first().stats.first().slot)
    }

    @Test
    fun `missing list entries are reported as an incomplete response`() = runTest {
        server.enqueue(
            jsonResponse(
                """
                {
                  "count": 2,
                  "results": [
                    {"name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon/1/"}
                  ]
                }
                """.trimIndent()
            )
        )
        val dataSource = createDataSource(catalogSize = 2)

        val error = runCatching { dataSource.fetchCatalog() }.exceptionOrNull()

        assertTrue(error is PokemonRemoteException.IncompleteResponse)
    }

    @Test
    fun `http errors are exposed as server errors`() = runTest {
        server.enqueue(MockResponse().setResponseCode(503))
        val dataSource = createDataSource(catalogSize = 2)

        val error = runCatching { dataSource.fetchCatalog() }.exceptionOrNull()

        assertTrue(error is PokemonRemoteException.Server)
    }

    private fun createDataSource(catalogSize: Int): RetrofitPokemonRemoteDataSource {
        val json = Json { ignoreUnknownKeys = true }
        val service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PokeApiService::class.java)
        return RetrofitPokemonRemoteDataSource(
            service = service,
            catalogSize = catalogSize,
            maxConcurrentRequests = 2,
        )
    }

    private fun pokemonDispatcher(): Dispatcher =
        object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                when {
                    request.path?.startsWith("/pokemon?limit=2") == true -> jsonResponse(
                        """
                        {
                          "count": 2,
                          "results": [
                            {"name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon/1/"},
                            {"name": "ivysaur", "url": "https://pokeapi.co/api/v2/pokemon/2/"}
                          ]
                        }
                        """.trimIndent()
                    )

                    request.path == "/pokemon/bulbasaur" -> pokemonResponse(
                        id = 1,
                        name = "bulbasaur",
                        imageUrl = "https://images.test/bulbasaur.png",
                    )

                    request.path == "/pokemon/ivysaur" -> pokemonResponse(
                        id = 2,
                        name = "ivysaur",
                        imageUrl = "https://images.test/ivysaur.png",
                    )

                    else -> MockResponse().setResponseCode(404)
                }
        }

    private fun pokemonResponse(
        id: Int,
        name: String,
        imageUrl: String,
    ): MockResponse =
        jsonResponse(
            """
            {
              "id": $id,
              "name": "$name",
              "height": 7,
              "weight": 69,
              "is_default": true,
              "abilities": [
                {
                  "is_hidden": false,
                  "slot": 1,
                  "ability": {"name": "overgrow", "url": "https://pokeapi.co/api/v2/ability/65/"}
                }
              ],
              "sprites": {
                "front_default": "https://images.test/front.png",
                "other": {
                  "official-artwork": {"front_default": "$imageUrl"}
                }
              },
              "stats": [
                {
                  "base_stat": 45,
                  "stat": {"name": "hp", "url": "https://pokeapi.co/api/v2/stat/1/"}
                }
              ],
              "types": [
                {
                  "slot": 1,
                  "type": {"name": "grass", "url": "https://pokeapi.co/api/v2/type/12/"}
                }
              ]
            }
            """.trimIndent()
        )

    private fun jsonResponse(body: String): MockResponse =
        MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody(body)
}
