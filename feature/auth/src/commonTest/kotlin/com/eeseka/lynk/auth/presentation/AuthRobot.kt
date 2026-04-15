package com.eeseka.lynk.auth.presentation

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.eeseka.lynk.shared.domain.auth.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalTestApi::class)
class AuthRobot(private val composeTestRule: ComposeUiTest) {

    fun setContent(
        state: AuthState = AuthState(),
        events: Flow<AuthEvent> = emptyFlow(),
        onAction: (AuthAction) -> Unit = {},
        onAuthSuccess: (User) -> Unit = {}
    ) = apply {
        composeTestRule.setContent {
            AuthScreen(
                state = state,
                events = events,
                onAction = onAction,
                onAuthSuccess = onAuthSuccess
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