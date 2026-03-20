package com.theminimaldev.gymprint.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gym_location")
data class GymLocation(
    @PrimaryKey val id: Int = 1, // single row
    val name: String,
    val lat: Double,
    val lng: Double,
    val radiusM: Float = 100f
)
