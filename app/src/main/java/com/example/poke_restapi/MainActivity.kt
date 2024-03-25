package com.example.poke_restapi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.IOException
import okhttp3.Request
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    private lateinit var generateButton: Button
    private lateinit var imageView: ImageView
    private lateinit var nameView: TextView
    private lateinit var abilityView: TextView

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        generateButton = findViewById(R.id.button)
        imageView = findViewById(R.id.imageView3)
        nameView = findViewById(R.id.textView3)
        abilityView = findViewById(R.id.textView)

        generateButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val pokemonId = Random.nextInt(1, 898) // Assuming there are 897 Pokémon available
                    val pokemonData = fetchPokemonData(pokemonId.toString())
                    val (name, imageUrl, ability) = extractPokemonInfo(pokemonData)

                    withContext(Dispatchers.Main) {
                        updateUI(name, imageUrl, ability)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        // Show error message on the main thread
                        Toast.makeText(this@MainActivity, "Error fetching Pokémon data", Toast.LENGTH_LONG).show()
                    }
                    e.printStackTrace()
                }
            }
        }
    }


    private suspend fun fetchPokemonData(pokemonName: String): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://pokeapi.co/api/v2/pokemon/${pokemonName.lowercase()}")
            .build()


        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                response.body?.string() ?: throw IOException("Null Response Body")
            }
        }
    }

    private fun extractPokemonInfo(pokemonData: String): Triple<String, String, String> {
        val jsonObject = JSONObject(pokemonData)

        val name = jsonObject.getString("name")
        val imageUrl = jsonObject.getJSONObject("sprites").getString("front_default")
        val ability = jsonObject.getJSONArray("abilities")
            .getJSONObject(0)
            .getJSONObject("ability")
            .getString("name")

        return Triple(name, imageUrl, ability)
    }


    private fun updateUI(name: String, imageUrl: String, ability: String) {
        nameView.text = "Name: $name"
        abilityView.text = "Ability: $ability"
        Picasso.get().load(imageUrl).into(imageView)
        Picasso.get()
            .load(imageUrl)
            .fit()
            .centerInside()
            .into(imageView)

    }
}