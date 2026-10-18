package com.example.doomscroller

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_starts_on_onboarding_and_navigates_to_dashboard_and_choose_apps() {
        // Assert Onboarding screen is visible
        composeTestRule.onNodeWithText("Welcome to Less-Mobile World").assertExists()

        // Click to go to PermissionSetup
        composeTestRule.onNodeWithText("Get Started").performClick()
        
        // Assert Permission Setup screen is visible
        composeTestRule.onNodeWithText("Permissions Required").assertExists()
        
        // Click to continue to Dashboard
        composeTestRule.onNodeWithText("Continue").performClick()

        // Assert Dashboard is visible
        composeTestRule.onNodeWithText("Dashboard").assertExists()

        // Click Add App
        composeTestRule.onNodeWithText("Add App").performClick()

        // Assert Choose Apps screen is visible
        composeTestRule.onNodeWithText("Choose Apps").assertExists()
    }
}
