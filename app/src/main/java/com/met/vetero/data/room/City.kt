package com.met.vetero.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class City(
    @PrimaryKey val id: Int,
    @ColumnInfo val name: String,
    @ColumnInfo val country : String
)
