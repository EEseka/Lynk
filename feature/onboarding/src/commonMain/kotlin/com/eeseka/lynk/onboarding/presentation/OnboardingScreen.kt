package com.eeseka.lynk.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.eeseka.lynk.onboarding.presentation.components.OnboardingControls
import com.eeseka.lynk.onboarding.presentation.components.OnboardingPageContent
import com.eeseka.lynk.onboarding.presentation.model.OnboardingPageUi
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.util.DeviceConfiguration
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import kotlinx.coroutines.launch
import lynk.feature.onboarding.generated.resources.Res
import lynk.feature.onboarding.generated.resources.discover_trending_spots_nearby
import lynk.feature.onboarding.generated.resources.end_the_debate
import lynk.feature.onboarding.generated.resources.everyone_pays_before_deadline
import lynk.feature.onboarding.generated.resources.find_the_perfect_spot
import lynk.feature.onboarding.generated.resources.split_the_cost_evenly
import lynk.feature.onboarding.generated.resources.stop_the_group_chat_debate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val ANIMATION_COMMUNITY = "community.json"
private const val ANIMATION_MAP_PIN_LOCATION = "map_pin_location.json"
private const val ANIMATION_PAYMENT_SUCCESS = "payment_success.json"

@Composable
fun OnboardingRoot(
    onNavigateToAuth: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            OnboardingEvent.Success -> onNavigateToAuth()
        }
    }

    OnboardingScreen(onAction = viewModel::onAction)
}

@Composable
fun OnboardingScreen(onAction: (OnboardingAction) -> Unit) {
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
            title = stringResource(Res.string.split_the_cost_evenly),
            description = stringResource(Res.string.everyone_pays_before_deadline),
            animationFileName = ANIMATION_PAYMENT_SUCCESS
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val hapticFeedback = rememberAppHaptic()

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

    val onButtonClick: () -> Unit = {
        if (pagerState.currentPage < pages.size - 1) {
            hapticFeedback(AppHaptic.Selection)
            scope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        } else {
            hapticFeedback(AppHaptic.ImpactMedium)
            onAction(OnboardingAction.OnGetStartedClick)
        }
    }

    LynkScaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (config) {
                DeviceConfiguration.MOBILE_LANDSCAPE -> {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f)
                        ) { pageIndex ->
                            OnboardingPageContent(page = pages[pageIndex])
                        }

                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentAlignment = Alignment.BottomCenter
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
                        modifier = Modifier.widthIn(max = 600.dp).fillMaxSize().padding(24.dp)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f)
                        ) { pageIndex ->
                            OnboardingPageContent(page = pages[pageIndex])
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
}

@PreviewLightDark
@Composable
private fun OnboardingScreenPreview() {
    LynkTheme {
        OnboardingScreen(onAction = {})
    }
}

@Preview(name = "Mobile landscape", widthDp = 900, heightDp = 400)
@Composable
private fun OnboardingScreenLandscapePreview() {
    LynkTheme {
        OnboardingScreen(onAction = {})
    }
}