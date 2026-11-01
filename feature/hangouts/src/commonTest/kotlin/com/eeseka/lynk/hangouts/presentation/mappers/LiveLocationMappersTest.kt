package com.eeseka.lynk.hangouts.presentation.mappers

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.model.LiveLocationUi
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class LiveLocationMappersTest {

    private val now = Instant.fromEpochMilliseconds(4102444800000L)

    @Test
    fun `leaves out you and anyone not going and anyone not sharing`() {
        val participants = listOf(
            participant("me"),
            participant("declined", RsvpStatus.DECLINED),
            participant("quiet"),
            participant("ada")
        )
        val liveLocations = mapOf(
            "me" to liveLocation(),
            "declined" to liveLocation(),
            "ada" to liveLocation()
        )

        val people = participants.toLiveParticipants(liveLocations, currentUserId = "me", now = now)

        assertThat(people.map { it.participant.user.userId }).containsExactly("ada")
    }

    @Test
    fun `counts whole minutes since the position was shared`() {
        val liveLocations = mapOf("ada" to liveLocation(age = 150.seconds.inWholeSeconds))

        val person = listOf(participant("ada")).toLiveParticipants(liveLocations, currentUserId = "me", now = now).single()

        assertThat(person.minutesSinceShared).isEqualTo(2)
    }

    @Test
    fun `a phone clock ahead of the server never gives negative minutes`() {
        val liveLocations = mapOf("ada" to liveLocation(age = -120))

        val person = listOf(participant("ada")).toLiveParticipants(liveLocations, currentUserId = "me", now = now).single()

        assertThat(person.minutesSinceShared).isEqualTo(0)
    }

    private fun liveLocation(age: Long = 0) = LiveLocationUi(
        latitude = 6.5,
        longitude = 3.3,
        sharedAt = now - age.seconds
    )

    private fun participant(userId: String, rsvpStatus: RsvpStatus = RsvpStatus.ATTENDING) = HangoutParticipantUi(
        user = HangoutUserUi(
            userId = userId,
            username = userId,
            displayName = userId,
            initials = "A",
            profilePictureUrl = null
        ),
        rsvpStatus = rsvpStatus,
        hasPaid = false,
        hasArrived = false
    )
}
