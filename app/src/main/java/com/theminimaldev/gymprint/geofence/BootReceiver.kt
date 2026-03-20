package com.theminimaldev.gymprint.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.theminimaldev.gymprint.data.repository.GymRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var gymRepository: GymRepository
    @Inject lateinit var geofenceManager: GeofenceManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        CoroutineScope(Dispatchers.IO).launch {
            val location = gymRepository.getGymLocation() ?: return@launch
            geofenceManager.register(location.lat, location.lng, location.radiusM)
        }
    }
}
