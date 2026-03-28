package com.eeseka.lynk.onboarding.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.eeseka.lynk.onboarding.presentation.components.OnboardingControls
import com.eeseka.lynk.onboarding.presentation.components.OnboardingPageContent
import com.eeseka.lynk.onboarding.presentation.model.OnboardingPageUi
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.presentation.util.DeviceConfiguration
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import kotlinx.coroutines.launch
import lynk.feature.onboarding.generated.resources.Res
import lynk.feature.onboarding.generated.resources.discover_trending_spots_nearby
import lynk.feature.onboarding.generated.resources.end_the_debate
import lynk.feature.onboarding.generated.resources.find_the_perfect_spot
import lynk.feature.onboarding.generated.resources.secure_your_reservation
import lynk.feature.onboarding.generated.resources.split_the_cost_instantly
import lynk.feature.onboarding.generated.resources.stop_the_group_chat_debate
import org.jetbrains.compose.resources.stringResource

private const val ANIMATION_COMMUNITY = "community.json"
private const val ANIMATION_MAP_PIN_LOCATION = "map_pin_location.json"
private const val ANIMATION_PAYMENT_SUCCESS = "payment_success.json"

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun OnboardingScreen() {
    val config = currentDeviceConfiguration()

    val pages = listOf(
        OnboardingPageUi(
            title = stringResource(Res.string.stop_the_group_chat_debate),
            description = stringResource(Res.string.end_the_debate),
            animationFileName = ANIMATION_COMMUNITY
        ),
        OnboardingPageUi(
            title = stringResource(Res.string.find_the_perfect_spot),
            description = stringResource(Res.string.discover_trending_spots_nearby),
            animationFileName = ANIMATION_MAP_PIN_LOCATION
        ),
        OnboardingPageUi(
            title = stringResource(Res.string.split_the_cost_instantly),
            description = stringResource(Res.string.secure_your_reservation),
            animationFileName = ANIMATION_PAYMENT_SUCCESS
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // If we are NOT on the first page, INTERCEPT the back button.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = pagerState.currentPage > 0,
        onBackCompleted = {
            scope.launch {
                // Instead of closing the app, scroll back one page
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }
        }
    )

    val hapticFeedback = rememberAppHaptic()

    val onButtonClick: () -> Unit = {
        if (pagerState.currentPage < pages.size - 1) {
            hapticFeedback(AppHaptic.Selection)
            scope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        } else {
            hapticFeedback(AppHaptic.Success)
            scope.launch {
                snackbarHostState.showFlashMessage(
                    message = "Onboarding Completed Successfully!",
                    type = LynkFlashType.Success
                )
            }
        }
    }

    LynkScaffold(
        snackbarHostState = snackbarHostState
    ) { paddingValues ->
        when (config) {
            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                Row(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { pageIndex ->
                        OnboardingPageContent(page = pages[pageIndex], isLandscape = true)
                    }

                    Column(
                        modifier = Modifier.weight(0.8f).fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OnboardingControls(
                            currentPage = pagerState.currentPage,
                            pageSize = pages.size,
                            onOnboardingButtonClick = onButtonClick
                        )
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .then(
                            if (config.isWideScreen || config == DeviceConfiguration.TABLET_PORTRAIT) {
                                Modifier.widthIn(max = 600.dp)
                            } else Modifier.fillMaxWidth()
                        )
                        .padding(paddingValues)
                        .padding(24.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { pageIndex ->
                        OnboardingPageContent(page = pages[pageIndex], isLandscape = false)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    OnboardingControls(
                        currentPage = pagerState.currentPage,
                        pageSize = pages.size,
                        onOnboardingButtonClick = onButtonClick
                    )
                }
            }
        }
    }
}