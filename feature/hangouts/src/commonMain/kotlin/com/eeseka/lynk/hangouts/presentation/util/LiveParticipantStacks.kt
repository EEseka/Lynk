package com.eeseka.lynk.hangouts.presentation.util

import com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.model.LiveParticipantUi
import com.eeseka.lynk.shared.presentation.spot.util.DistanceCalculator

// People close enough for their avatars to cover each other share one stack, seeded by whoever comes first
fun List<LiveParticipantUi>.toLiveParticipantStacks(radiusMeters: Double): List<List<LiveParticipantUi>> {
    val stacks = mutableListOf<MutableList<LiveParticipantUi>>()

    forEach { person ->
        val stack = stacks.firstOrNull { stack ->
            val seed = stack.first()
            DistanceCalculator.calculateDistanceInMeters(
                userLat = seed.latitude,
                userLng = seed.longitude,
                spotLat = person.latitude,
                spotLng = person.longitude
            ) <= radiusMeters
        }
        if (stack != null) stack.add(person) else stacks.add(mutableListOf(person))
    }

    return stacks
}
