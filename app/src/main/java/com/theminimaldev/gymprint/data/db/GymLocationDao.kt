package com.theminimaldev.gymprint.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GymLocationDao {
    @Query("SELECT * FROM gym_location WHERE id = 1 LIMIT 1")
    fun observe(): Flow<GymLocation?>

    @Query("SELECT * FROM gym_location WHERE id = 1 LIMIT 1")
    suspend fun get(): GymLocation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(location: GymLocation)
}
