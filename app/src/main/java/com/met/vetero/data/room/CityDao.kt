package com.met.vetero.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CityDao {
    @Query("SELECT * FROM city WHERE name LIKE '%' || :name || '%'")
    fun findCityByName(name : String) : List<City>

    @Query("SELECT * FROM city")
    fun getAll() : List<City>

    @Insert
    fun insertCity(city: City)
}