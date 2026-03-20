package com.theminimaldev.gymprint.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theminimaldev.gymprint.BuildConfig

enum class OnboardingStep {
    WELCOME,
    LOCATION_PICKER,
    FOREGROUND_PERMISSION,
    BACKGROUND_RATIONALE,
    BACKGROUND_PERMISSION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var step by remember { mutableStateOf(OnboardingStep.WELCOME) }

    LaunchedEffect(Unit) {
        viewModel.onboardingComplete.collect { onComplete() }
    }

    val foregroundPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            step = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                OnboardingStep.BACKGROUND_RATIONALE
            else {
                viewModel.onConfirmLocation()
                OnboardingStep.WELCOME // will trigger navigation via effect
            }
        }
    }

    val backgroundPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Whether granted or not, proceed — geofencing degrades gracefully
        viewModel.onConfirmLocation()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
    AnimatedContent(
        targetState = step,
        label = "onboarding_step"
    ) { currentStep ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentStep) {
                OnboardingStep.WELCOME -> WelcomeStep(
                    onContinue = { step = OnboardingStep.LOCATION_PICKER }
                )

                OnboardingStep.LOCATION_PICKER -> {
                    if (BuildConfig.HAS_PLACES_API) {
                        LocationPickerStep(
                            viewModel = viewModel,
                            onConfirm = { step = OnboardingStep.FOREGROUND_PERMISSION }
                        )
                    } else {
                        ManualLocationStep(
                            viewModel = viewModel,
                            onConfirm = { step = OnboardingStep.FOREGROUND_PERMISSION }
                        )
                    }
                }

                OnboardingStep.FOREGROUND_PERMISSION -> {
                    LaunchedEffect(Unit) {
                        foregroundPermLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                OnboardingStep.BACKGROUND_RATIONALE -> BackgroundRationaleStep(
                    onContinue = { step = OnboardingStep.BACKGROUND_PERMISSION }
                )

                OnboardingStep.BACKGROUND_PERMISSION -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        LaunchedEffect(Unit) {
                            backgroundPermLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }
                    }
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
    } // end safeDrawingPadding Box
    } // end Surface
}

@Composable
private fun WelcomeStep(onContinue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = "Welcome to\nGymprint",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "We automatically track your gym visits so you don't have to. Just go lift — we'll handle the rest.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(48.dp))
        FilledTonalButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Let's go", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun BackgroundRationaleStep(onContinue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.MyLocation,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "One more thing",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "To detect gym visits when the app is closed, Gymprint needs background location access.\n\nWe only check your location when you enter a specific area you set up — nothing else, ever. The app is open source, so you can verify this yourself.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(48.dp))
        FilledTonalButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Grant background access", style = MaterialTheme.typography.labelLarge)
        }
    }
}
