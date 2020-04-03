package com.met.vetero.presentation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.met.vetero.data.api.WeatherResponse
import com.met.vetero.data.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel(private val repository: WeatherRepository) : ViewModel() {

    val weatherResponse = MutableLiveData<WeatherResponse>()
    val error = MutableLiveData<Throwable>()

    fun fetchWeatherForecast(lat : String, lon : String, apiKey : String) {
        val scope = CoroutineScope(Dispatchers.Main).launch {
            val result  = withContext(Dispatchers.IO) {
                runCatching {
                    repository.getForecast(lat, lon, apiKey)
                }
            }

            result.onSuccess {
                Log.i("SUCCESS", "Weather info  : $it")
                weatherResponse.value = it
            }
            result.onFailure { error.value = it }

        }
    }

}