package com.example.doomscroller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.doomscroller.data.PreferencesManager
import com.example.doomscroller.domain.EnforcementEvent
import com.example.doomscroller.domain.EnforcementManager
import com.example.doomscroller.platform.PermissionManager
import com.example.doomscroller.platform.UsageMonitoringService
import com.example.doomscroller.ui.navigation.DoomscrollerNavGraph
import com.example.doomscroller.ui.navigation.Screen
import com.example.doomscroller.ui.theme.DoomscrollerTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val pendingDestination = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        val prefs = PreferencesManager(this)
        val permissionManager = PermissionManager(this)

        // Compute start destination dynamically based on live system authorization and onboarding state
        val initialStartRoute = when {
            !prefs.isOnboardingCompleted() -> Screen.Onboarding.route
            !permissionManager.hasUsageAccess() -> Screen.PermissionSetup.route
            else -> Screen.Dashboard.route
        }

        Log.i(TAG, "MainActivity onCreate: initialRoute=$initialStartRoute (onboardingCompleted=${prefs.isOnboardingCompleted()}, hasUsageAccess=${permissionManager.hasUsageAccess()})")

        setContent {
            DoomscrollerTheme {
                val navController = rememberNavController()
                val destination by pendingDestination
                val enforcementManager = remember { EnforcementManager.getInstance(applicationContext) }

                // Navigate to incoming target (e.g., from notification full-screen / click)
                LaunchedEffect(destination) {
                    destination?.let { dest ->
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute != dest) {
                            Log.i(TAG, "Navigating to target destination: $dest (current=$currentRoute)")
                            navController.navigate(dest) {
                                launchSingleTop = true
                            }
                        }
                        pendingDestination.value = null
                    }
                }

                // Listen for in-app warning triggers to display learning gate screen without duplication
                LaunchedEffect(Unit) {
                    enforcementManager.enforcementEvents.collect { event ->
                        if (event is EnforcementEvent.WarningTriggered) {
                            val targetRoute = Screen.LearningGate.createRoute(event.packageName)
                            val currentRoute = navController.currentBackStackEntry?.destination?.route
                            if (currentRoute != targetRoute) {
                                Log.i(TAG, "In-app WarningTriggered received for ${event.packageName}. Navigating to $targetRoute")
                                navController.navigate(targetRoute) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                }

                DoomscrollerNavGraph(
                    navController = navController,
                    startDestination = initialStartRoute,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val permissionManager = PermissionManager(this)
        if (permissionManager.hasUsageAccess()) {
            UsageMonitoringService.start(this)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val dest = intent.getStringExtra("destination")
        Log.i(TAG, "handleIntent: action=$action, dest=$dest")

        val rawPackage = intent.getStringExtra("packageName") ?: ""
        val sanitizedPackage = if (rawPackage.matches(Regex("^[a-zA-Z0-9_.]+$"))) {
            rawPackage
        } else {
            "unknown"
        }

        if (action == "com.example.doomscroller.ACTION_LEARNING_GATE" || dest == "learning_gate") {
            // Security verification: verify this package is actively monitored and restricted
            val prefs = PreferencesManager(this)
            val session = prefs.getReminderSession(sanitizedPackage)
            val rule = prefs.getRule(sanitizedPackage)
            if (rule != null && rule.isEnabled && (session.isEnforcementActive || session.isLearningGateCompleted)) {
                pendingDestination.value = Screen.LearningGate.createRoute(sanitizedPackage)
            } else {
                val anyActive = prefs.getActivePenalty()
                if (anyActive != null) {
                    pendingDestination.value = Screen.LearningGate.createRoute(anyActive.packageName)
                } else {
                    Log.w(TAG, "Ignored Learning Gate navigation for unmonitored or inactive package: $sanitizedPackage")
                }
            }
        } else if (action == "com.example.doomscroller.ACTION_INTERRUPTION" || dest == "interruption") {
            val prefs = PreferencesManager(this)
            val rule = prefs.getRule(sanitizedPackage)
            if (rule != null && rule.isEnabled) {
                pendingDestination.value = Screen.Interruption.createRoute(sanitizedPackage)
            } else {
                Log.w(TAG, "Ignored Interruption navigation for unmonitored package: $sanitizedPackage")
            }
        }
    }
}
