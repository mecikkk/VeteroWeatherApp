package com.met.vetero.data.repository

import com.met.vetero.data.api.OpenWeatherApi
import com.met.vetero.data.api.WeatherResponse

class WeatherRepository(private val api : OpenWeatherApi) {

    suspend fun getForecast(lat : String, lon : String, appid : String) : WeatherResponse {
        return api.getWeatherByCoordinates(lat,lon,appid)
    }

}