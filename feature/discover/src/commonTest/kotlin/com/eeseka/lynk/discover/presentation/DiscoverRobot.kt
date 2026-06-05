package com.eeseka.lynk.discover.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalTestApi::class)
class DiscoverRobot(private val composeTestRule: ComposeUiTest) {

    fun setContent(
        state: DiscoverState = DiscoverState(),
        events: Flow<DiscoverEvent> = emptyFlow(),
        onAction: (DiscoverAction) -> Unit = {},
        navigateToHangouts: (String) -> Unit = {}
    ) = apply {
        composeTestRule.setContent {
            DiscoverScreen(
                state = state,
                events = events,
                onAction = onAction,
                navigateToHangouts = navigateToHangouts,
                mainShellPadding = PaddingValues(0.dp)
            )
        }
    }

    // --- Search interactions ---
    fun clickSearchField(placeholderText: String) = apply {
        composeTestRule.onNodeWithText(placeholderText).performClick()
    }

    fun assertEmptySearchStateVisible(emptyMessage: String) = apply {
        composeTestRule.onNodeWithText(emptyMessage).assertIsDisplayed()
    }

    fun clickCategoryFilter(categoryTitle: String) = apply {
        composeTestRule.onNodeWithText(categoryTitle).performClick()
    }

    // --- Spot Cards & Sheets ---
    fun assertSpotNameVisible(spotName: String) = apply {
        composeTestRule.onNodeWithText(spotName).assertIsDisplayed()
    }

    fun clickSpotCard(spotName: String) = apply {
        composeTestRule.onNodeWithText(spotName).performClick()
    }

    fun assertSpotDetailSheetVisible(aboutText: String) = apply {
        composeTestRule.onNodeWithText(aboutText).assertIsDisplayed()
    }

    // --- Map Buttons ---
    fun clickLocateMeButton(contentDesc: String) = apply {
        composeTestRule.onNodeWithContentDescription(contentDesc).performClick()
    }
}