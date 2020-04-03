package com.met.vetero.data.entities

import com.google.gson.annotations.SerializedName
import java.sql.Date

data class WeatherInfo(
    @SerializedName("main") val temperature : Temperature,
    val weather : List<Weather>,
    val clouds : Clouds,
    val wind : Wind,
    @SerializedName("dt_txt") val date : Date
)
