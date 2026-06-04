package com.eeseka.lynk.discover.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DiscoverScreenTest {

    private val dummySpot = SpotUi(
        id = "1", name = "Test Cafe", category = SpotCategory.CAFE,
        latitude = 0.0, longitude = 0.0, isSaved = false,
        photoUrls = emptyList(), rating = 4.5, reviewCount = 10,
        isOpenNow = true, shortAddress = "123 Main St",
        websiteUrl = null, googleMapsUrl = null, priceLevel = null,
        description = null, tags = emptyList()
    )

    @Test
    fun `empty search results show empty state UI in sheet`() = runComposeUiTest {
        val robot = DiscoverRobot(this)

        val state = DiscoverState(
            searchResults = emptyList(),
            searchEndReached = true,
            isSearchLoading = false,
            selectedCategory = SpotCategory.CAFE
        )

        robot
            .setContent(state = state)
            .clickSearchField("Search for a spot...")
            .assertEmptySearchStateVisible("Try adjusting your filters or using different keywords.")
    }

    @Test
    fun `search results display spot cards in grid`() = runComposeUiTest {
        val robot = DiscoverRobot(this)

        val state = DiscoverState(
            searchResults = listOf(dummySpot),
            selectedCategory = SpotCategory.CAFE
        )

        robot
            .setContent(state = state)
            .clickSearchField("Search for a spot...")
            .assertSpotNameVisible("Test Cafe")
    }

    @Test
    fun `selecting a spot opens spot detail sheet`() = runComposeUiTest {
        val robot = DiscoverRobot(this)

        val state = DiscoverState(
            trendingSpots = listOf(dummySpot),
            selectedSpotId = "1"
        )

        robot
            .setContent(state = state)
            .assertSpotDetailSheetVisible("About this spot")
            .assertSpotNameVisible("Test Cafe")
    }
}