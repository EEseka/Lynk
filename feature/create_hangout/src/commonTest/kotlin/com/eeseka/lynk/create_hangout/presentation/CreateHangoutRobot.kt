package com.eeseka.lynk.create_hangout.presentation

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalTestApi::class)
class CreateHangoutRobot(private val composeTestRule: ComposeUiTest) {

    fun setContent(
        state: CreateHangoutState = CreateHangoutState(),
        events: Flow<CreateHangoutEvent> = emptyFlow(),
        onAction: (CreateHangoutAction) -> Unit = {},
        onSuccess: (String) -> Unit = {},
        onDismissRequest: () -> Unit = {}
    ) = apply {
        composeTestRule.setContent {
            CreateHangoutSheet(
                state = state,
                events = events,
                onAction = onAction,
                onSuccess = onSuccess,
                onDismissRequest = onDismissRequest
            )
        }
    }

    fun assertTitleVisible(title: String) = apply {
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    fun assertNextButtonEnabled(text: String) = apply {
        composeTestRule.onNodeWithText(text).assertIsEnabled()
    }

    fun assertNextButtonDisabled(text: String) = apply {
        composeTestRule.onNodeWithText(text).assertIsNotEnabled()
    }

    fun assertTextVisible(text: String) = apply {
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    fun clickButton(text: String) = apply {
        composeTestRule.onNodeWithText(text).performClick()
    }
}