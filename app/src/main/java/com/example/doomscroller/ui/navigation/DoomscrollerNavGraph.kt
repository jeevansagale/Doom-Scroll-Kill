package com.example.doomscroller.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.doomscroller.platform.PermissionManager
import com.example.doomscroller.ui.AppViewModel
import com.example.doomscroller.ui.screens.AppRuleEditorScreen
import com.example.doomscroller.ui.screens.ChooseAppsScreen
import com.example.doomscroller.ui.screens.DashboardScreen
import com.example.doomscroller.ui.screens.HistorySettingsScreen
import com.example.doomscroller.ui.screens.InterruptionScreen
import com.example.doomscroller.ui.screens.LearningGateScreen
import com.example.doomscroller.ui.screens.OnboardingScreen
import com.example.doomscroller.ui.screens.PermissionSetupScreen
import com.example.doomscroller.ui.screens.RecommendationGuidanceScreen

@Composable
fun DoomscrollerNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Onboarding.route
) {
    val appViewModel: AppViewModel = viewModel()
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(
                onContinueClick = {
                    appViewModel.setOnboardingCompleted(true)
                    if (permissionManager.hasUsageAccess()) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.PermissionSetup.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(route = Screen.PermissionSetup.route) {
            val hasUsageAccess by appViewModel.hasUsageAccess.collectAsState()
            val hasNotificationPermission by appViewModel.hasNotificationPermission.collectAsState()

            PermissionSetupScreen(
                hasUsageAccess = hasUsageAccess,
                hasNotificationPermission = hasNotificationPermission,
                onUsageAccessClick = { 
                    appViewModel.checkPermissions()
                },
                onNotificationClick = {
                    appViewModel.checkPermissions()
                },
                onRefreshPermissions = {
                    appViewModel.checkPermissions()
                },
                onContinueClick = {
                    appViewModel.setOnboardingCompleted(true)
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.PermissionSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = appViewModel,
                onAddAppClick = {
                    navController.navigate(Screen.ChooseApps.route)
                },
                onAppClick = { packageName ->
                    navController.navigate(Screen.AppRuleEditor.createRoute(packageName))
                },
                onSettingsClick = {
                    navController.navigate(Screen.HistorySettings.route)
                },
                onRecommendationGuidanceClick = {
                    navController.navigate(Screen.RecommendationGuidance.route)
                }
            )
        }

        composable(route = Screen.ChooseApps.route) {
            ChooseAppsScreen(
                viewModel = appViewModel,
                onAppSelected = { packageName ->
                    navController.navigate(Screen.AppRuleEditor.createRoute(packageName)) {
                        popUpTo(Screen.ChooseApps.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.AppRuleEditor.route) { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
            AppRuleEditorScreen(
                packageName = packageName,
                viewModel = appViewModel,
                onSaveClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.HistorySettings.route) {
            HistorySettingsScreen(viewModel = appViewModel)
        }

        composable(route = Screen.Interruption.route) { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
            InterruptionScreen(
                packageName = packageName,
                onLeaveAppClick = {
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(homeIntent)
                    (context as? Activity)?.moveTaskToBack(true)
                },
                onContinueClick = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.LearningGate.route) { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
            LearningGateScreen(
                packageName = packageName,
                viewModel = appViewModel,
                onLeaveAppClick = {
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(homeIntent)
                    (context as? Activity)?.moveTaskToBack(true)
                },
                onGateCompleted = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.RecommendationGuidance.route) {
            RecommendationGuidanceScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
