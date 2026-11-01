package com.eeseka.lynk.hangouts.presentation.util

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.model.LiveParticipantUi
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import kotlin.test.Test

class LiveParticipantStacksTest {

    @Test
    fun `people within the radius share a stack`() {
        val stacks = listOf(person("ada", 6.5000), person("bola", 6.5001)).toLiveParticipantStacks(radiusMeters = 50.0)

        assertThat(stacks.map { stack -> stack.map { it.participant.user.userId } })
            .containsExactly(listOf("ada", "bola"))
    }

    @Test
    fun `people beyond the radius stay apart`() {
        val stacks = listOf(person("ada", 6.50), person("bola", 6.51)).toLiveParticipantStacks(radiusMeters = 50.0)

        assertThat(stacks.map { stack -> stack.map { it.participant.user.userId } })
            .containsExactly(listOf("ada"), listOf("bola"))
    }

    @Test
    fun `nobody sharing makes no stacks`() {
        assertThat(emptyList<LiveParticipantUi>().toLiveParticipantStacks(radiusMeters = 50.0)).isEmpty()
    }

    private fun person(userId: String, latitude: Double) = LiveParticipantUi(
        participant = HangoutParticipantUi(
            user = HangoutUserUi(
                userId = userId,
                username = userId,
                displayName = userId,
                initials = "A",
                profilePictureUrl = null
            ),
            rsvpStatus = RsvpStatus.ATTENDING,
            hasPaid = false,
            hasArrived = false
        ),
        latitude = latitude,
        longitude = 3.3,
        minutesSinceShared = 0
    )
}
