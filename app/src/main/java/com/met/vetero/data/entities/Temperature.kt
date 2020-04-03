package com.met.vetero.data.entities

import com.google.gson.annotations.SerializedName

data class Temperature(
    @SerializedName("temp") val current : Double,
    @SerializedName("feels_like") val feelsLike : Double,
    val pressure : Int,
    val humidity : Int
)
