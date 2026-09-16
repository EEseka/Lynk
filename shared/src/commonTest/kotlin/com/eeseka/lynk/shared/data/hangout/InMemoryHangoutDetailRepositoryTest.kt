package com.eeseka.lynk.shared.data.hangout

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.testing.data.FakeHangoutService
import com.eeseka.lynk.testing.data.FakeSessionStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

class InMemoryHangoutDetailRepositoryTest {

    private lateinit var hangoutService: FakeHangoutService
    private lateinit var sessionStorage: FakeSessionStorage

    @BeforeTest
    fun setUp() {
        hangoutService = FakeHangoutService()
        sessionStorage = FakeSessionStorage()
    }

    @Test
    fun `a hangout is null until a refresh succeeds`() = runTest {
        val repository = createRepository()
        val hangoutId = createHangout("Night Out")

        assertThat(repository.observeHangout(hangoutId).first()).isNull()
    }

    @Test
    fun `a refresh stores the hangout for everyone observing it`() = runTest {
        val repository = createRepository()
        val hangoutId = createHangout("Night Out")

        val result = repository.refreshHangout(hangoutId)

        assertThat(result).isInstanceOf(Result.Success::class)
        assertThat(repository.observeHangout(hangoutId).first()?.name).isEqualTo("Night Out")
    }

    @Test
    fun `a failed refresh keeps the last copy and returns the error`() = runTest {
        val repository = createRepository()
        val hangoutId = createHangout("Night Out")
        repository.refreshHangout(hangoutId)

        hangoutService.shouldReturnError = true
        val result = repository.refreshHangout(hangoutId)

        assertThat(result).isEqualTo(Result.Failure(DataError.Remote.SERVER_ERROR))
        assertThat(repository.observeHangout(hangoutId).first()?.name).isEqualTo("Night Out")
    }

    @Test
    fun `hangouts are kept apart by id`() = runTest {
        val repository = createRepository()
        val refreshedId = createHangout("Night Out")
        val otherId = createHangout("Brunch")

        repository.refreshHangout(refreshedId)

        assertThat(repository.observeHangout(otherId).first()).isNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a change of user clears every stored hangout`() = runTest {
        sessionStorage.set(guestSession(userId = "user_a"))
        val repository = createRepository()
        val hangoutId = createHangout("Night Out")
        repository.refreshHangout(hangoutId)

        sessionStorage.set(guestSession(userId = "user_b"))
        runCurrent()

        assertThat(repository.observeHangout(hangoutId).first()).isNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.createRepository(): InMemoryHangoutDetailRepository {
        val repository = InMemoryHangoutDetailRepository(hangoutService, sessionStorage, backgroundScope)
        runCurrent()
        return repository
    }

    private suspend fun createHangout(name: String): String {
        hangoutService.createHangout(
            name = name,
            description = null,
            vibe = HangoutVibe.CHILL,
            scheduledAt = Instant.fromEpochMilliseconds(4102444800000L),
            maxAttendees = null,
            spotId = null
        )
        return hangoutService.hangouts.last().id
    }

    private fun guestSession(userId: String) = AuthInfo(
        accessToken = "access",
        refreshToken = "refresh",
        user = User.Guest(id = userId)
    )
}
