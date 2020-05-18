package com.met.vetero.presentation

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.met.vetero.data.api.WeatherResponse
import com.met.vetero.data.repository.WeatherRepository
import com.met.vetero.data.room.CitiesDatabase
import com.met.vetero.data.room.City
import com.met.vetero.data.room.CityDao
import com.met.vetero.utils.Const
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class MainActivityViewModel(private val repository: WeatherRepository) : ViewModel() {

    val weatherResponse = MutableLiveData<WeatherResponse>()
    val city = MutableLiveData<City>()
    val allCities = MutableLiveData<List<City>>()
    val locationFromGps = MutableLiveData<Boolean>()
    val apiError = MutableLiveData<Throwable>()
    var isGpsModeOn = false
    var connectedToInternet = false
    private var db: CitiesDatabase? = null
    private var dao: CityDao? = null

    fun fetchWeatherForecast(
        lat: String,
        lon: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    repository.getForecast(lat, lon)
                }
            }

            result.onSuccess {
                weatherResponse.value = it
                locationFromGps.value = true
            }
            result.onFailure { apiError.value = it }

        }
    }

    fun fetchWeatherForecast(
        cityId: Int
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    repository.getForecast(cityId)
                }
            }

            result.onSuccess {
                weatherResponse.value = it
                locationFromGps.value = false
            }
            result.onFailure { apiError.value = it }
        }
    }

    suspend fun findCityInJsonFile(
        activity: Activity,
        query: String
    ) {
        val listOfCities = mutableListOf<City>()
        db = Room.databaseBuilder(activity, CitiesDatabase::class.java, "cities_database")
                .build()
        dao = db?.cityDao()

        withContext(Dispatchers.IO) {
            listOfCities.addAll(dao!!.findCityByName(query))
        }

        withContext(Dispatchers.Main) {
            allCities.value = listOfCities
        }
        db?.close()
    }

    suspend fun createDatabaseIfNotExist(
        context: Context,
        sp: SharedPreferences
    ) {
        db = Room.databaseBuilder(context, CitiesDatabase::class.java, "cities_database")
                .build()
        dao = db?.cityDao()

        if (!sp.getBoolean(Const.SHARED_PREF_DATABASE_EXIST, false)) {

            val editor = sp.edit()
            editor.putBoolean(Const.SHARED_PREF_DATABASE_EXIST, true)
            editor.apply()

            withContext(Dispatchers.IO) {

                val inputStream = context.assets.open("city.list.json")

                val reader = JsonReader(InputStreamReader(inputStream, Charsets.UTF_8))

                reader.beginArray()

                while (reader.hasNext()) {
                    val singleCity = Gson().fromJson<City>(reader, City::class.java)
                    dao?.insertCity(singleCity)
                }
                reader.endArray()
                reader.close()
                inputStream.close()
            }

            db?.close()
        }
    }

    fun storeLocationInSharedPreferences(
        sp: SharedPreferences,
        cityId: Int
    ) {
        val editor = sp.edit()
        editor.putInt(Const.SHARED_PREF_SAVED_LOCATION, cityId)
        editor.apply()
    }

    fun getLocationFromSharedPreferences(sp: SharedPreferences): Int = sp.getInt(Const.SHARED_PREF_SAVED_LOCATION, -1)

}