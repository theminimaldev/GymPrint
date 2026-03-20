package com.theminimaldev.gymprint.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GymVisitDao {
    @Query("SELECT * FROM gym_visit ORDER BY date DESC")
    fun observeAll(): Flow<List<GymVisit>>

    @Query("SELECT * FROM gym_visit ORDER BY date DESC")
    suspend fun getAll(): List<GymVisit>

    // INSERT OR IGNORE — duplicate dates silently dropped
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(visit: GymVisit): Long

    @Query("DELETE FROM gym_visit")
    suspend fun deleteAll()
}
