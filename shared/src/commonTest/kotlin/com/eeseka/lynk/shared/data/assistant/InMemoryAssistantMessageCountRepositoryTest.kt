package com.eeseka.lynk.shared.data.assistant

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessage
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessageRole
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.testing.data.FakeAssistantService
import com.eeseka.lynk.testing.data.FakeSessionStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

class InMemoryAssistantMessageCountRepositoryTest {

    private lateinit var assistantService: FakeAssistantService
    private lateinit var sessionStorage: FakeSessionStorage

    @BeforeTest
    fun setUp() {
        assistantService = FakeAssistantService()
        sessionStorage = FakeSessionStorage()
    }

    @Test
    fun `the message count is unknown until a refresh succeeds`() = runTest {
        val repository = createRepository()

        assertThat(repository.observeMessageCount(HANGOUT_ID).first()).isNull()
    }

    @Test
    fun `a refresh stores the message count`() = runTest {
        assistantService.hangoutMessages = mutableListOf(question())
        val repository = createRepository()

        val result = repository.refreshMessageCount(HANGOUT_ID)

        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(repository.observeMessageCount(HANGOUT_ID).first()).isEqualTo(1L)
    }

    @Test
    fun `a refresh the user is no longer allowed drops what was known`() = runTest {
        assistantService.hangoutMessages = mutableListOf(question())
        val repository = createRepository()
        repository.refreshMessageCount(HANGOUT_ID)

        assistantService.shouldReturnError = true
        assistantService.errorToReturn = DataError.Remote.FORBIDDEN
        repository.refreshMessageCount(HANGOUT_ID)

        assertThat(repository.observeMessageCount(HANGOUT_ID).first()).isNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a change of user clears what was known`() = runTest {
        sessionStorage.set(guestSession(userId = "user_a"))
        assistantService.hangoutMessages = mutableListOf(question())
        val repository = createRepository()
        repository.refreshMessageCount(HANGOUT_ID)

        sessionStorage.set(guestSession(userId = "user_b"))
        runCurrent()

        assertThat(repository.observeMessageCount(HANGOUT_ID).first()).isNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.createRepository(): InMemoryAssistantMessageCountRepository {
        val repository = InMemoryAssistantMessageCountRepository(assistantService, sessionStorage, backgroundScope)
        runCurrent()
        return repository
    }

    private fun question() = AssistantMessage(
        id = "message_1",
        role = AssistantMessageRole.USER,
        status = null,
        content = "Somewhere chill",
        failureReason = null,
        recommendations = emptyList(),
        createdAt = Instant.fromEpochMilliseconds(1700000000000L)
    )

    private fun guestSession(userId: String) = AuthInfo(
        accessToken = "access",
        refreshToken = "refresh",
        user = User.Guest(id = userId)
    )

    private companion object {
        const val HANGOUT_ID = "hangout_1"
    }
}
