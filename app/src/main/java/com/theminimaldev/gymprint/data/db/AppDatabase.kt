package com.theminimaldev.gymprint.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GymLocation::class, GymVisit::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gymLocationDao(): GymLocationDao
    abstract fun gymVisitDao(): GymVisitDao
}
