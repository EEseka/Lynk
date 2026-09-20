package com.eeseka.lynk.shared.data.notification

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.notification.model.Notification
import com.eeseka.lynk.shared.domain.notification.model.NotificationType
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.testing.data.FakeAppLifecycleObserver
import com.eeseka.lynk.testing.data.FakeNotificationService
import com.eeseka.lynk.testing.data.FakeSessionStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryUnreadNotificationCounterTest {

    private lateinit var notificationService: FakeNotificationService
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var appLifecycleObserver: FakeAppLifecycleObserver

    @BeforeTest
    fun setUp() {
        notificationService = FakeNotificationService()
        sessionStorage = FakeSessionStorage()
        appLifecycleObserver = FakeAppLifecycleObserver()
    }

    @Test
    fun `coming back to the app counts the unread notifications`() = runTest {
        signIn()
        notificationService.notifications = mutableListOf(unread("n1"), unread("n2"))
        val counter = createCounter()

        appLifecycleObserver.isInForeground.value = true
        runCurrent()

        assertThat(counter.count.value).isEqualTo(2L)
    }

    @Test
    fun `a guest is never counted`() = runTest {
        sessionStorage.set(session(User.Guest(id = "guest_1")))
        notificationService.notifications = mutableListOf(unread("n1"))
        val counter = createCounter()

        appLifecycleObserver.isInForeground.value = true
        runCurrent()

        assertThat(counter.count.value).isEqualTo(0L)
    }

    @Test
    fun `going to the background does not count again`() = runTest {
        signIn()
        notificationService.notifications = mutableListOf(unread("n1"))
        val counter = createCounter()
        appLifecycleObserver.isInForeground.value = true
        runCurrent()

        notificationService.notifications = mutableListOf(unread("n1"), unread("n2"))
        appLifecycleObserver.isInForeground.value = false
        runCurrent()

        assertThat(counter.count.value).isEqualTo(1L)
    }

    @Test
    fun `reading one notification lowers the badge`() = runTest {
        signIn()
        notificationService.notifications = mutableListOf(unread("n1"), unread("n2"))
        val counter = createCounter()
        appLifecycleObserver.isInForeground.value = true
        runCurrent()

        counter.decrement()

        assertThat(counter.count.value).isEqualTo(1L)
    }

    @Test
    fun `a push arriving while the app is open raises the badge`() = runTest {
        signIn()
        notificationService.notifications = mutableListOf(unread("n1"))
        val counter = createCounter()
        appLifecycleObserver.isInForeground.value = true
        runCurrent()

        counter.increment()

        assertThat(counter.count.value).isEqualTo(2L)
    }

    @Test
    fun `the badge never goes below zero`() = runTest {
        signIn()
        val counter = createCounter()

        counter.decrement()

        assertThat(counter.count.value).isEqualTo(0L)
    }

    @Test
    fun `a change of user empties the badge and counts again`() = runTest {
        signIn(userId = "user_1")
        notificationService.notifications = mutableListOf(unread("n1"), unread("n2"))
        val counter = createCounter()
        appLifecycleObserver.isInForeground.value = true
        runCurrent()

        notificationService.notifications = mutableListOf(unread("n3"))
        signIn(userId = "user_2")
        runCurrent()

        assertThat(counter.count.value).isEqualTo(1L)
    }

    @Test
    fun `signing out empties the badge`() = runTest {
        signIn()
        notificationService.notifications = mutableListOf(unread("n1"))
        val counter = createCounter()
        appLifecycleObserver.isInForeground.value = true
        runCurrent()

        sessionStorage.set(null)
        runCurrent()

        assertThat(counter.count.value).isEqualTo(0L)
    }

    private fun TestScope.createCounter(): InMemoryUnreadNotificationCounter {
        val counter = InMemoryUnreadNotificationCounter(
            notificationService = notificationService,
            sessionStorage = sessionStorage,
            appLifecycleObserver = appLifecycleObserver,
            applicationScope = backgroundScope
        )
        runCurrent()
        return counter
    }

    private suspend fun signIn(userId: String = "user_1") {
        sessionStorage.set(
            session(
                User.Authenticated(
                    id = userId,
                    provider = AuthProvider.GOOGLE,
                    email = "$userId@test.com",
                    displayName = userId,
                    username = userId
                )
            )
        )
    }

    private fun session(user: User) = AuthInfo(
        accessToken = "access",
        refreshToken = "refresh",
        user = user
    )

    private fun unread(id: String) = Notification(
        id = id,
        type = NotificationType.SPOT_CHOSEN,
        hangoutId = "hangout_1",
        hangoutName = "Night Out",
        actorDisplayName = "Ada",
        amountKobo = null,
        isRead = false,
        createdAt = Instant.fromEpochMilliseconds(4102444800000L)
    )
}
