package com.eeseka.lynk.auth.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

@OptIn(ExperimentalTestApi::class)
class AuthRobot(private val composeTestRule: ComposeUiTest) {

    fun setContent(
        state: AuthState = AuthState(),
        onAction: (AuthAction) -> Unit = {},
        snackbarHostState: SnackbarHostState = SnackbarHostState()
    ) = apply {
        composeTestRule.setContent {
            AuthScreen(
                state = state,
                onAction = onAction,
                snackbarHostState = snackbarHostState
            )
        }
    }

    fun assertButtonEnabled(text: String) = apply {
        composeTestRule.onNodeWithText(text).assertIsEnabled()
    }

    fun assertButtonDisabled(text: String) = apply {
        composeTestRule.onNodeWithText(text).assertIsNotEnabled()
    }

    fun assertTextVisible(text: String) = apply {
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    fun clickButton(text: String) = apply {
        composeTestRule.onNodeWithText(text).performClick()
    }
}