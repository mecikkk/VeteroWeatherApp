package com.met.vetero.data.entities

import com.google.gson.annotations.SerializedName

data class Clouds (
    @SerializedName("all") val cloudinessLevel : Int
)
