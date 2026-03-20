package com.theminimaldev.gymprint.data.repository

import com.theminimaldev.gymprint.data.db.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GymRepository @Inject constructor(
    private val gymLocationDao: GymLocationDao,
    private val gymVisitDao: GymVisitDao
) {
    // --- Location ---
    fun observeGymLocation(): Flow<GymLocation?> = gymLocationDao.observe()
    suspend fun getGymLocation(): GymLocation? = gymLocationDao.get()
    suspend fun saveGymLocation(location: GymLocation) = gymLocationDao.upsert(location)

    // --- Visits ---
    fun observeVisits(): Flow<List<GymVisit>> = gymVisitDao.observeAll()
    suspend fun getAllVisits(): List<GymVisit> = gymVisitDao.getAll()

    /** Upsert today's visit — INSERT OR IGNORE, so duplicate dates are dropped silently. */
    suspend fun recordVisitToday() {
        val today = LocalDate.now().toString()
        gymVisitDao.insertIfAbsent(GymVisit(date = today))
    }

    /** Record a visit for an explicit date string (yyyy-MM-dd). */
    suspend fun recordVisit(date: String) {
        gymVisitDao.insertIfAbsent(GymVisit(date = date))
    }
}
