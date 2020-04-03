package com.met.vetero.data.entities

data class City(

    val name : String,
    val coord : Coord,
    val country : String,
    val sunrise : Long,
    val sunset : Long

)
