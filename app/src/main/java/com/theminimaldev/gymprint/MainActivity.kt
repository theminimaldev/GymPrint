package com.theminimaldev.gymprint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.libraries.places.api.Places
import com.theminimaldev.gymprint.data.datastore.DataStoreManager
import com.theminimaldev.gymprint.ui.home.HomeScreen
import com.theminimaldev.gymprint.ui.navigation.Screen
import com.theminimaldev.gymprint.ui.onboarding.OnboardingScreen
import com.theminimaldev.gymprint.ui.settings.SettingsScreen
import com.theminimaldev.gymprint.ui.theme.GymprintTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (BuildConfig.HAS_PLACES_API && !Places.isInitialized()) {
            val apiKey = packageManager
                .getApplicationInfo(packageName, android.content.pm.PackageManager.GET_META_DATA)
                .metaData?.getString("com.google.android.geo.API_KEY") ?: ""
            if (apiKey.isNotBlank()) {
                Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
            }
        }

        val isOnboardingComplete = runBlocking {
            dataStoreManager.isOnboardingComplete.first()
        }

        setContent {
            GymprintTheme {
                val navController = rememberNavController()
                val startDest = if (isOnboardingComplete) Screen.Home.route else Screen.Onboarding.route

                NavHost(navController = navController, startDestination = startDest) {
                    composable(Screen.Onboarding.route) {
                        OnboardingScreen(
                            onComplete = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Screen.Home.route) {
                        HomeScreen(onNavigateToSettings = { navController.navigate(Screen.Settings.route) })
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onChangeGymLocation = {
                                navController.navigate(Screen.Onboarding.route) {
                                    popUpTo(Screen.Home.route)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
