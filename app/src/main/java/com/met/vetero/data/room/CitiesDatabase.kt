package com.met.vetero.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(City::class), version = 1)
abstract class CitiesDatabase : RoomDatabase() {
    abstract fun cityDao() : CityDao
}