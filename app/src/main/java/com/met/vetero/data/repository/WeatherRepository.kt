package com.met.vetero.data.repository

import com.met.vetero.data.api.OpenWeatherApi
import com.met.vetero.data.api.WeatherResponse

class WeatherRepository(private val api : OpenWeatherApi) {

    suspend fun getForecast(lat : String, lon : String, appid : String) : WeatherResponse {
        return api.getWeatherByCoordinates(lat,lon,appid)
    }

    suspend fun getForecast(cityName : String, appid: String) : WeatherResponse {
        return api.getWeatherByCityName(cityName, appid)
    }

    suspend fun getForecast(cityId : Int, appid: String) : WeatherResponse {
        return api.getWeatherByCityId(cityId, appid)
    }

}