package com.theminimaldev.gymprint.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theminimaldev.gymprint.data.datastore.DataStoreManager
import com.theminimaldev.gymprint.data.repository.GymRepository
import com.theminimaldev.gymprint.geofence.GeofenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val gymName: String = "",
    val minVisitDurationMinutes: Int = 20,
    val geofenceRadiusMeters: Float = 100f
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val gymRepository: GymRepository,
    private val geofenceManager: GeofenceManager
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        gymRepository.observeGymLocation(),
        dataStoreManager.minVisitDurationMinutes,
        dataStoreManager.geofenceRadiusMeters
    ) { location, duration, radius ->
        SettingsUiState(
            gymName = location?.name ?: "",
            minVisitDurationMinutes = duration,
            geofenceRadiusMeters = radius
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setMinVisitDuration(minutes: Int) {
        viewModelScope.launch { dataStoreManager.setMinVisitDurationMinutes(minutes) }
    }

    fun setGeofenceRadius(meters: Float) {
        viewModelScope.launch {
            dataStoreManager.setGeofenceRadiusMeters(meters)
            val loc = gymRepository.getGymLocation() ?: return@launch
            geofenceManager.register(loc.lat, loc.lng, meters)
        }
    }
}
