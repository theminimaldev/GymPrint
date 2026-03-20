package com.theminimaldev.gymprint.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onChangeGymLocation: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var visible by remember { mutableStateOf(false) }
    var durationText by remember(state.minVisitDurationMinutes) {
        mutableStateOf(state.minVisitDurationMinutes.toString())
    }
    var radiusText by remember(state.geofenceRadiusMeters) {
        mutableStateOf(state.geofenceRadiusMeters.toInt().toString())
    }

    LaunchedEffect(Unit) {
        delay(80)
        visible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(200)) + slideInVertically(
                    spring(Spring.DampingRatioMediumBouncy),
                    initialOffsetY = { it / 3 }
                )
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Section header: Location
                    Text(
                        text = "LOCATION",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                    )

                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        ListItem(
                            headlineContent = { Text("Gym location") },
                            supportingContent = {
                                Text(
                                    state.gymName.ifEmpty { "Not set" },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingContent = {
                                Icon(Icons.Rounded.LocationOn, contentDescription = null)
                            },
                            trailingContent = {
                                FilledTonalButton(onClick = onChangeGymLocation) {
                                    Text("Change")
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Section header: Detection
                    Text(
                        text = "DETECTION",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                    )

                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column {
                            ListItem(
                                headlineContent = { Text("Minimum visit duration") },
                                supportingContent = { Text("Visits shorter than this are ignored") },
                                trailingContent = {
                                    OutlinedTextField(
                                        value = durationText,
                                        onValueChange = { v ->
                                            durationText = v
                                            v.toIntOrNull()?.let { viewModel.setMinVisitDuration(it) }
                                        },
                                        suffix = { Text("min") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.width(100.dp),
                                        singleLine = true
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                            ListItem(
                                headlineContent = { Text("Geofence radius") },
                                supportingContent = { Text("Detection area around your gym") },
                                trailingContent = {
                                    OutlinedTextField(
                                        value = radiusText,
                                        onValueChange = { v ->
                                            radiusText = v
                                            v.toFloatOrNull()?.let { viewModel.setGeofenceRadius(it) }
                                        },
                                        suffix = { Text("m") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.width(100.dp),
                                        singleLine = true
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
