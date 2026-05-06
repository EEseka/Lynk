package com.eeseka.lynk.discover.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DiscoverScreenTest {

    private val dummySpot = Spot(
        id = "1", name = "Test Cafe", category = SpotCategory.CAFE,
        latitude = 0.0, longitude = 0.0, isSaved = false,
        photoUrls = emptyList(), rating = 4.5, reviewCount = 10,
        isOpenNow = true, shortAddress = "123 Main St",
        websiteUrl = null, googleMapsUrl = null, priceLevel = null,
        description = null, tags = emptyList(), savedAt = null
    )

    @Test
    fun `empty search results show empty state UI in sheet`() = runComposeUiTest {
        val robot = DiscoverRobot(this)
        
        // Simulating an active search that returned nothing
        val state = DiscoverState(
            searchResults = emptyList(),
            searchEndReached = true,
            isSearchLoading = false,
            selectedCategory = SpotCategory.CAFE // Activates search mode
        )

        robot
            .setContent(state = state)
            .clickSearchField("Search for a spot...") // Opens sheet
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
            .clickSearchField("Search for a spot...") // Opens sheet
            .assertSpotNameVisible("Test Cafe")
    }

    @Test
    fun `selecting a spot opens spot detail sheet`() = runComposeUiTest {
        val robot = DiscoverRobot(this)
        
        // Set state with a selected spot ID
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