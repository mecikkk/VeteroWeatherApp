package com.met.vetero.presentation

import android.app.Activity
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.met.vetero.data.api.WeatherResponse
import com.met.vetero.data.entities.City
import com.met.vetero.data.entities.Coord
import com.met.vetero.data.repository.WeatherRepository
import com.met.vetero.utils.Const
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.nio.BufferOverflowException
import java.util.zip.GZIPInputStream

class MainActivityViewModel(private val repository: WeatherRepository) : ViewModel() {

    val weatherResponse = MutableLiveData<WeatherResponse>()
    val city = MutableLiveData<City>()
    val allCities = MutableLiveData<List<City>>()
    val apiError = MutableLiveData<Throwable>()
    var connectedToInternet = false

    fun fetchWeatherForecast(lat: String, lon: String, apiKey: String) {
        if (weatherResponse.value == null) {
            CoroutineScope(Dispatchers.Main).launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        repository.getForecast(lat, lon, apiKey)
                    }
                }

                result.onSuccess { weatherResponse.value = it }
                result.onFailure { apiError.value = it }

            }
        }
    }

    fun fetchWeatherForecast(cityName: String, apiKey: String) {
        if (weatherResponse.value == null) {
            CoroutineScope(Dispatchers.Main).launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        repository.getForecast(cityName, apiKey)
                    }
                }

                result.onSuccess {
                    city.value = it.city
                }
                result.onFailure { apiError.value = it }

            }
        }
    }

    fun fetchWeatherForecast(cityId: Int, apiKey: String) {
            CoroutineScope(Dispatchers.Main).launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        repository.getForecast(cityId, apiKey)
                    }
                }

                result.onSuccess {weatherResponse.value = it
                }
                result.onFailure { apiError.value = it }
            }
    }

    suspend fun findCityInJsonFile(activity : Activity, query : String) {
        val listOfCities = mutableListOf<City>()

        withContext(Dispatchers.IO){

            val inputStream = activity.applicationContext.assets.open("city.list.json")
            val bufferReader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

            var i = 0
            val allLines = bufferReader.readLines()
            allLines.forEach {
                if(it.contains(query, true)){
                    val id = allLines[i-1].substringAfter("\"id\": ").substringBefore(",").toInt()
                    val name = allLines[i].substringAfter("\"name\": \"").substringBefore("\",")
                    val country = allLines[i+2].substringAfter("\"country\": \"").substringBefore("\",")
                    val lon = allLines[i+4].substringAfter("\"lon\": ").substringBefore(",")
                    val lat = allLines[i+5].substringAfter("\"lat\": ")
                    listOfCities.add(City(id = id, name = name, coord = Coord(lat, lon), country = country, sunrise = 0, sunset = 0))
                }
                i++
            }

            inputStream.close()

        }

        withContext(Dispatchers.Main) {
            allCities.value = listOfCities
            Log.d("JSON ARRAY", "ALL CITIES : ${allCities.value.toString()}")
        }
    }

    fun storeLocationInSharedPreferences(sp : SharedPreferences, cityId : Int) {
        val editor = sp.edit()
        editor.putInt(Const.SHARED_PREF_SAVED_LOCATION, cityId)
        editor.apply()
    }

    fun getStoredLocation(sp : SharedPreferences) : Int = sp.getInt(Const.SHARED_PREF_SAVED_LOCATION, -1)


}


