package com.met.vetero.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.met.vetero.data.api.WeatherResponse
import com.met.vetero.data.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchCityViewModel(private val repository: WeatherRepository) : ViewModel() {

    val weatherResponse = MutableLiveData<WeatherResponse>()
    val apiError = MutableLiveData<Throwable>()

    fun fetchWeatherForecast(cityName : String, apiKey: String) {
        if (weatherResponse.value == null) {
            CoroutineScope(Dispatchers.Main).launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        repository.getForecast(cityName, apiKey)
                    }
                }

                result.onSuccess { weatherResponse.value = it }
                result.onFailure { apiError.value = it }

            }
        }
    }
}