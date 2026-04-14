package com.eeseka.lynk.profile_setup.presentation

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalTestApi::class)
class ProfileSetupRobot(private val composeTestRule: ComposeUiTest) {

    fun setContent(
        state: ProfileSetupState = ProfileSetupState(),
        events: Flow<ProfileSetupEvent> = emptyFlow(),
        onAction: (ProfileSetupAction) -> Unit = {}
    ) = apply {
        composeTestRule.setContent {
            ProfileSetupScreen(
                state = state,
                events = events,
                onAction = onAction,
                onProfileSetupComplete = {}
            )
        }
    }

    // --- Buttons & Form ---
    fun assertSubmitButtonDisabled(buttonText: String) = apply {
        composeTestRule.onNodeWithText(buttonText).assertIsNotEnabled()
    }

    fun assertSubmitButtonEnabled(buttonText: String) = apply {
        composeTestRule.onNodeWithText(buttonText).assertIsEnabled()
    }

    fun assertSubmitButtonLoading(loadingText: String) = apply {
        composeTestRule.onNodeWithText(loadingText).assertIsDisplayed()
        composeTestRule.onNodeWithText(loadingText).assertIsNotEnabled()
    }

    fun clickSubmit(buttonText: String) = apply {
        composeTestRule.onNodeWithText(buttonText).performClick()
    }

    fun assertErrorTextVisible(errorText: String) = apply {
        composeTestRule.onNodeWithText(errorText).assertIsDisplayed()
    }

    // --- Username Dynamic Icons ---
    fun assertUsernameAvailableIconVisible(availableDesc: String) = apply {
        composeTestRule.onNodeWithContentDescription(availableDesc).assertIsDisplayed()
    }

    fun assertUsernameUnavailableIconVisible(unavailableDesc: String) = apply {
        composeTestRule.onNodeWithContentDescription(unavailableDesc).assertIsDisplayed()
    }

    // --- Avatar & Action Sheet ---
    fun clickAvatarCircle(chooseDesc: String) = apply {
        // Taps the camera icon or the placeholder circle
        composeTestRule.onNodeWithContentDescription(chooseDesc).performClick()
    }

    fun assertActionSheetVisible(sheetTitle: String) = apply {
        composeTestRule.onNodeWithText(sheetTitle).assertIsDisplayed()
    }

    fun clickActionSheetItem(itemText: String) = apply {
        composeTestRule.onNodeWithText(itemText).performClick()
    }

    // --- Snackbar ---
    fun assertSnackbarMessageVisible(message: String) = apply {
        composeTestRule.onNodeWithText(message).assertIsDisplayed()
    }
}