package com.met.vetero.data.entities

data class City(

    val id: Int,
    val name: String,
    val coord: Coord,
    val country: String,
    val sunrise: Long = 0,
    val sunset: Long = 0

)
