package com.example.doomscroller.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object ChooseApps : Screen("choose_apps")
    object AppRuleEditor : Screen("app_rule_editor/{packageName}") {
        fun createRoute(packageName: String): String {
            val safePackage = if (packageName.isBlank()) "unknown" else java.net.URLEncoder.encode(packageName, "UTF-8")
            return "app_rule_editor/$safePackage"
        }
    }
    object PermissionSetup : Screen("permission_setup")
    object HistorySettings : Screen("history_settings")
    object Interruption : Screen("interruption/{packageName}") {
        fun createRoute(packageName: String): String {
            val safePackage = if (packageName.isBlank()) "unknown" else java.net.URLEncoder.encode(packageName, "UTF-8")
            return "interruption/$safePackage"
        }
    }
    object LearningGate : Screen("learning_gate/{packageName}") {
        fun createRoute(packageName: String): String {
            val safePackage = if (packageName.isBlank()) "unknown" else java.net.URLEncoder.encode(packageName, "UTF-8")
            return "learning_gate/$safePackage"
        }
    }
    object RecommendationGuidance : Screen("recommendation_guidance")
    object Discovery : Screen("discovery")
}
