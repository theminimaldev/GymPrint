package com.theminimaldev.gymprint.ui.onboarding

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "LocationPicker"

@Composable
fun LocationPickerStep(
    viewModel: OnboardingViewModel,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // Prevents re-searching after a place is picked
    var suppressSearch by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        if (suppressSearch) { suppressSearch = false; return@LaunchedEffect }
        if (searchQuery.length < 2) {
            predictions = emptyList()
            searched = false
            errorMessage = null
            return@LaunchedEffect
        }
        delay(400)
        if (!Places.isInitialized()) {
            errorMessage = "Places not initialized"
            return@LaunchedEffect
        }
        isSearching = true
        errorMessage = null
        try {
            val client = Places.createClient(context)
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(searchQuery)
                .build()
            predictions = suspendCancellableCoroutine { cont ->
                client.findAutocompletePredictions(request)
                    .addOnSuccessListener { cont.resume(it.autocompletePredictions) }
                    .addOnFailureListener {
                        Log.e(TAG, "Autocomplete failed", it)
                        cont.resumeWithException(it)
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search error", e)
            predictions = emptyList()
            errorMessage = e.message ?: "Search failed"
        } finally {
            isSearching = false
            searched = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        // Icon in colored circle
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.MyLocation,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Where do you lift?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Search for your gym by name or address.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                predictions = emptyList()
                searched = false
                errorMessage = null
            },
            placeholder = { Text("e.g. Woohoo, Bangalore") },
            leadingIcon = {
                if (isSearching)
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else
                    Icon(Icons.Rounded.Search, contentDescription = null)
            },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        errorMessage?.let { msg ->
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (errorMessage == null && searched && predictions.isEmpty() && !isSearching) {
            Text(
                text = "No results — try a different name or address",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Predictions list
        if (predictions.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                    items(predictions) { prediction ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    prediction.getPrimaryText(null).toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            supportingContent = {
                                Text(
                                    prediction.getSecondaryText(null).toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            modifier = Modifier.clickable {
                                scope.launch {
                                    try {
                                        val client = Places.createClient(context)
                                        val fields = listOf(Place.Field.DISPLAY_NAME, Place.Field.LOCATION)
                                        val req = FetchPlaceRequest.newInstance(prediction.placeId, fields)
                                        val place = suspendCancellableCoroutine<Place> { cont ->
                                            client.fetchPlace(req)
                                                .addOnSuccessListener { cont.resume(it.place) }
                                                .addOnFailureListener {
                                                    Log.e(TAG, "FetchPlace failed", it)
                                                    cont.resumeWithException(it)
                                                }
                                        }
                                        val latLng = place.location ?: return@launch
                                        val name = place.displayName
                                            ?: prediction.getPrimaryText(null).toString()
                                        viewModel.onPlaceSelected(
                                            PlaceResult(name = name, lat = latLng.latitude, lng = latLng.longitude)
                                        )
                                        // Clear search without re-triggering search
                                        suppressSearch = true
                                        searchQuery = ""
                                        predictions = emptyList()
                                        errorMessage = null
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Place selection error", e)
                                        errorMessage = e.message ?: "Failed to get place details"
                                    }
                                }
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Selected gym card
        selectedPlace?.let { place ->
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = place.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "%.5f, %.5f".format(place.lat, place.lng),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onConfirm,
            enabled = selectedPlace != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Icon(Icons.Rounded.LocationOn, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (selectedPlace != null) "This is my gym" else "Search for your gym first",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
