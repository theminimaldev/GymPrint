package com.theminimaldev.gymprint.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gym_visit",
    indices = [Index(value = ["date"], unique = true)]
)
data class GymVisit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String // ISO format yyyy-MM-dd
)
