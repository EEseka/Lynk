package com.eeseka.lynk.create_hangout.presentation

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

@OptIn(ExperimentalTestApi::class)
class CreateHangoutRobot(private val composeTestRule: ComposeUiTest) {

    fun setContent(
        state: CreateHangoutState = CreateHangoutState(),
        onAction: (CreateHangoutAction) -> Unit = {},
        onDismissRequest: () -> Unit = {}
    ) = apply {
        composeTestRule.setContent {
            CreateHangoutSheet(
                state = state,
                onAction = onAction,
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