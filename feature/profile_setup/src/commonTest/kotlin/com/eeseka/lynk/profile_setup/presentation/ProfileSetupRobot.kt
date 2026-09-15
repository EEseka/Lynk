package com.eeseka.lynk.profile_setup.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

@OptIn(ExperimentalTestApi::class)
class ProfileSetupRobot(private val composeTestRule: ComposeUiTest) {

    fun setContent(
        state: ProfileSetupState = ProfileSetupState(),
        onAction: (ProfileSetupAction) -> Unit = {},
        snackbarHostState: SnackbarHostState = SnackbarHostState()
    ) = apply {
        composeTestRule.setContent {
            ProfileSetupScreen(
                state = state,
                onAction = onAction,
                snackbarHostState = snackbarHostState
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