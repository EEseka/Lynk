package com.eeseka.lynk.onboarding.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.eeseka.lynk.testing.data.FakeOnboardingStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var onboardingStorage: FakeOnboardingStorage
    private lateinit var viewModel: OnboardingViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        onboardingStorage = FakeOnboardingStorage()
        viewModel = OnboardingViewModel(onboardingStorage)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getting started remembers that onboarding is done`() = runTest {
        viewModel.onAction(OnboardingAction.OnGetStartedClick)
        advanceUntilIdle()

        assertThat(onboardingStorage.hasSeenOnboarding.first()).isTrue()
    }

    @Test
    fun `getting started moves on`() = runTest {
        viewModel.events.test {
            viewModel.onAction(OnboardingAction.OnGetStartedClick)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(OnboardingEvent.Success)
        }
    }
}
