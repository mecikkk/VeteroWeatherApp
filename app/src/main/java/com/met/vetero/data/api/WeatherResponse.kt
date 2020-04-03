package com.met.vetero.data.api

import com.google.gson.annotations.SerializedName
import com.met.vetero.data.entities.City
import com.met.vetero.data.entities.WeatherInfo

data class WeatherResponse(
    @SerializedName("list") val forecastList: List<WeatherInfo>,
    val city: City
)