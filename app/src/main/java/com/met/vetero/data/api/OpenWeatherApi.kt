package com.met.vetero.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherApi {

    @GET("forecast")
    suspend fun getWeatherByCoordinates(
        @Query("lat") lat : String,
        @Query("lon") lon : String,
        @Query("appid") appId : String,
        @Query("lang") lang : String = "pl",
        @Query("units") metric : String = "metric"
    ) : WeatherResponse

}