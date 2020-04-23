package com.met.vetero.data.entities

import com.google.gson.annotations.SerializedName

data class Weather(
    @SerializedName("main") val name: String,
    val description: String,
    @SerializedName("icon") val iconId: String
)