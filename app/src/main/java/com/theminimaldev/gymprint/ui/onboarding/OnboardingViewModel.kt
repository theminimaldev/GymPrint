package com.theminimaldev.gymprint.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theminimaldev.gymprint.data.datastore.DataStoreManager
import com.theminimaldev.gymprint.data.db.GymLocation
import com.theminimaldev.gymprint.data.repository.GymRepository
import com.theminimaldev.gymprint.geofence.GeofenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaceResult(
    val name: String,
    val lat: Double,
    val lng: Double
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val gymRepository: GymRepository,
    private val dataStoreManager: DataStoreManager,
    private val geofenceManager: GeofenceManager
) : ViewModel() {

    private val _selectedPlace = MutableStateFlow<PlaceResult?>(null)
    val selectedPlace: StateFlow<PlaceResult?> = _selectedPlace.asStateFlow()

    private val _onboardingComplete = MutableSharedFlow<Unit>()
    val onboardingComplete: SharedFlow<Unit> = _onboardingComplete.asSharedFlow()

    fun onPlaceSelected(place: PlaceResult) {
        _selectedPlace.value = place
    }

    fun onConfirmLocation(radiusMeters: Float = 100f) {
        val place = _selectedPlace.value ?: return
        viewModelScope.launch {
            val location = GymLocation(
                name = place.name,
                lat = place.lat,
                lng = place.lng,
                radiusM = radiusMeters
            )
            gymRepository.saveGymLocation(location)
            geofenceManager.register(place.lat, place.lng, radiusMeters)
            dataStoreManager.setOnboardingComplete(true)
            _onboardingComplete.emit(Unit)
        }
    }
}
