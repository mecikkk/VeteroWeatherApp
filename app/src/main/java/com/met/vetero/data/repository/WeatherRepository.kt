package com.met.vetero.data.repository

import com.met.vetero.BuildConfig
import com.met.vetero.data.api.OpenWeatherApi
import com.met.vetero.data.api.WeatherResponse

class WeatherRepository(private val api: OpenWeatherApi) {

    suspend fun getForecast(
        lat: String,
        lon: String
    ): WeatherResponse {
        return api.getWeatherByCoordinates(lat, lon, BuildConfig.API_KEY)
    }

    suspend fun getForecast(
        cityId: Int
    ): WeatherResponse {
        return api.getWeatherByCityId(cityId, BuildConfig.API_KEY)
    }

}