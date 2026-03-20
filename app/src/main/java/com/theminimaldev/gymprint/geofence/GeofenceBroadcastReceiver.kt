package com.theminimaldev.gymprint.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.theminimaldev.gymprint.data.datastore.DataStoreManager
import com.theminimaldev.gymprint.data.repository.GymRepository
import com.theminimaldev.gymprint.widget.WidgetRefreshWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var dataStoreManager: DataStoreManager
    @Inject lateinit var gymRepository: GymRepository

    // 8 hours in milliseconds — stale ENTER threshold
    private val staleThresholdMs = 8 * 60 * 60 * 1000L

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            android.util.Log.e("GeofenceReceiver", "Geofence error: $errorMessage")
            return
        }

        val transition = geofencingEvent.geofenceTransition
        val scope = CoroutineScope(Dispatchers.IO)

        when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                scope.launch {
                    val savedEntry = dataStoreManager.getEntryTimestampMs()
                    val now = System.currentTimeMillis()
                    // If there's a stale entry older than 8 hours, discard it
                    if (savedEntry > 0 && (now - savedEntry) >= staleThresholdMs) {
                        dataStoreManager.clearEntryTimestamp()
                    }
                    // Only save a new entry if no current one is active
                    if (dataStoreManager.getEntryTimestampMs() == 0L) {
                        dataStoreManager.saveEntryTimestamp(now)
                    }
                }
            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                scope.launch {
                    val entryMs = dataStoreManager.getEntryTimestampMs()
                    if (entryMs == 0L) return@launch

                    val now = System.currentTimeMillis()
                    val durationMinutes = (now - entryMs) / 1000 / 60
                    val minDuration = dataStoreManager.getMinVisitDurationMinutes()

                    if (durationMinutes >= minDuration) {
                        // Record for the date the session started
                        val visitDate = java.time.Instant.ofEpochMilli(entryMs)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                            .toString()
                        gymRepository.recordVisit(visitDate)
                        // Trigger widget refresh
                        WidgetRefreshWorker.enqueueOnce(context)
                    }

                    dataStoreManager.clearEntryTimestamp()
                }
            }
        }
    }
}
