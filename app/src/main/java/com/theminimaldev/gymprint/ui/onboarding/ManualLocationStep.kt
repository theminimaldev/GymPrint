package com.theminimaldev.gymprint.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ManualLocationStep(
    viewModel: OnboardingViewModel,
    onConfirm: () -> Unit
) {
    val selectedPlace by viewModel.selectedPlace.collectAsState()

    var name by remember { mutableStateOf("") }
    var latText by remember { mutableStateOf("") }
    var lngText by remember { mutableStateOf("") }
    var latError by remember { mutableStateOf(false) }
    var lngError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = "Where do you lift?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Enter your gym's coordinates.\nYou can find them by long-pressing\nyour gym's location in Google Maps.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Gym name") },
            placeholder = { Text("e.g. Iron Paradise") },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = latText,
                onValueChange = {
                    latText = it
                    latError = false
                },
                label = { Text("Latitude") },
                placeholder = { Text("28.6139") },
                singleLine = true,
                isError = latError,
                supportingText = if (latError) {{ Text("Invalid") }} else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = lngText,
                onValueChange = {
                    lngText = it
                    lngError = false
                },
                label = { Text("Longitude") },
                placeholder = { Text("77.2090") },
                singleLine = true,
                isError = lngError,
                supportingText = if (lngError) {{ Text("Invalid") }} else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Confirm button
        FilledTonalButton(
            onClick = {
                val lat = latText.toDoubleOrNull()
                val lng = lngText.toDoubleOrNull()
                val validLat = lat != null && lat in -90.0..90.0
                val validLng = lng != null && lng in -180.0..180.0

                latError = !validLat
                lngError = !validLng

                if (validLat && validLng) {
                    val gymName = name.ifBlank { "My Gym" }
                    viewModel.onPlaceSelected(
                        PlaceResult(name = gymName, lat = lat!!, lng = lng!!)
                    )
                }
            },
            enabled = latText.isNotBlank() && lngText.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Set location", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(Modifier.height(12.dp))

        // Selected gym confirmation card
        selectedPlace?.let { place ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Rounded.LocationOn, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Column {
                        Text(
                            text = place.name,
                            style = MaterialTheme.typography.bodyMedium,
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

        FilledTonalButton(
            onClick = onConfirm,
            enabled = selectedPlace != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Rounded.LocationOn, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (selectedPlace != null) "This is my gym" else "Enter coordinates first",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
