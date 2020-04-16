package com.met.vetero.data.entities

data class Location(
    val id : Int,
    val name : String,
    val state : String,
    val country : String,
    val coord : Coord
)