package com.eeseka.lynk.shared.data.hangout

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhotoStats
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.testing.data.FakeHangoutPhotoService
import com.eeseka.lynk.testing.data.FakeSessionStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class InMemoryHangoutPhotoStatsRepositoryTest {

    private lateinit var hangoutPhotoService: FakeHangoutPhotoService
    private lateinit var sessionStorage: FakeSessionStorage

    @BeforeTest
    fun setUp() {
        hangoutPhotoService = FakeHangoutPhotoService()
        sessionStorage = FakeSessionStorage()
        hangoutPhotoService.myPhotoCount = 3
    }

    @Test
    fun `the counts are null until a refresh succeeds`() = runTest {
        val repository = createRepository()

        assertThat(repository.observePhotoStats(HANGOUT_ID).first()).isNull()
    }

    @Test
    fun `a refresh stores the counts for everyone observing them`() = runTest {
        val repository = createRepository()

        val result = repository.refreshPhotoStats(HANGOUT_ID)

        assertThat(result).isInstanceOf(Result.Success::class)
        assertThat(repository.observePhotoStats(HANGOUT_ID).first())
            .isEqualTo(HangoutPhotoStats(photoCount = 0, myPhotoCount = 3))
    }

    @Test
    fun `a failed refresh keeps the last counts and returns the error`() = runTest {
        val repository = createRepository()
        repository.refreshPhotoStats(HANGOUT_ID)

        hangoutPhotoService.shouldReturnError = true
        val result = repository.refreshPhotoStats(HANGOUT_ID)

        assertThat(result).isEqualTo(Result.Failure(DataError.Remote.SERVER_ERROR))
        assertThat(repository.observePhotoStats(HANGOUT_ID).first()?.myPhotoCount).isEqualTo(3)
    }

    @Test
    fun `a refresh the user is no longer allowed drops the last counts`() = runTest {
        val repository = createRepository()
        repository.refreshPhotoStats(HANGOUT_ID)

        hangoutPhotoService.shouldReturnError = true
        hangoutPhotoService.errorToReturn = DataError.Remote.FORBIDDEN
        repository.refreshPhotoStats(HANGOUT_ID)

        assertThat(repository.observePhotoStats(HANGOUT_ID).first()).isNull()
    }

    @Test
    fun `counts are kept apart by hangout id`() = runTest {
        val repository = createRepository()

        repository.refreshPhotoStats(HANGOUT_ID)

        assertThat(repository.observePhotoStats(OTHER_HANGOUT_ID).first()).isNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a change of user clears every stored count`() = runTest {
        sessionStorage.set(guestSession(userId = "user_a"))
        val repository = createRepository()
        repository.refreshPhotoStats(HANGOUT_ID)

        sessionStorage.set(guestSession(userId = "user_b"))
        runCurrent()

        assertThat(repository.observePhotoStats(HANGOUT_ID).first()).isNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.createRepository(): InMemoryHangoutPhotoStatsRepository {
        val repository = InMemoryHangoutPhotoStatsRepository(hangoutPhotoService, sessionStorage, backgroundScope)
        runCurrent()
        return repository
    }

    private fun guestSession(userId: String) = AuthInfo(
        accessToken = "access",
        refreshToken = "refresh",
        user = User.Guest(id = userId)
    )

    private companion object {
        const val HANGOUT_ID = "hangout_1"
        const val OTHER_HANGOUT_ID = "hangout_2"
    }
}
