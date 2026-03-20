package com.theminimaldev.gymprint.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "punchcard_prefs")

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val ENTRY_TIMESTAMP_MS = longPreferencesKey("entry_timestamp_ms")
        val MIN_VISIT_DURATION_MINUTES = intPreferencesKey("min_visit_duration_minutes")
        val GEOFENCE_RADIUS_METERS = floatPreferencesKey("geofence_radius_meters")
    }

    val isOnboardingComplete: Flow<Boolean> =
        context.dataStore.data.map { it[ONBOARDING_COMPLETE] ?: false }

    val entryTimestampMs: Flow<Long> =
        context.dataStore.data.map { it[ENTRY_TIMESTAMP_MS] ?: 0L }

    val minVisitDurationMinutes: Flow<Int> =
        context.dataStore.data.map { it[MIN_VISIT_DURATION_MINUTES] ?: 20 }

    val geofenceRadiusMeters: Flow<Float> =
        context.dataStore.data.map { it[GEOFENCE_RADIUS_METERS] ?: 100f }

    suspend fun setOnboardingComplete(value: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETE] = value }
    }

    suspend fun saveEntryTimestamp(epochMs: Long) {
        context.dataStore.edit { it[ENTRY_TIMESTAMP_MS] = epochMs }
    }

    suspend fun clearEntryTimestamp() {
        context.dataStore.edit { it.remove(ENTRY_TIMESTAMP_MS) }
    }

    suspend fun getEntryTimestampMs(): Long =
        context.dataStore.data.map { it[ENTRY_TIMESTAMP_MS] ?: 0L }.first()

    suspend fun setMinVisitDurationMinutes(minutes: Int) {
        context.dataStore.edit { it[MIN_VISIT_DURATION_MINUTES] = minutes }
    }

    suspend fun setGeofenceRadiusMeters(meters: Float) {
        context.dataStore.edit { it[GEOFENCE_RADIUS_METERS] = meters }
    }

    suspend fun getMinVisitDurationMinutes(): Int =
        context.dataStore.data.map { it[MIN_VISIT_DURATION_MINUTES] ?: 20 }.first()
}
