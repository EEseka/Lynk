package com.eeseka.lynk.hangouts.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun rememberHasTimePassed(epochMilliseconds: Long?): Boolean {
    val hasPassed by produceState(
        initialValue = epochMilliseconds != null && Clock.System.now().toEpochMilliseconds() >= epochMilliseconds,
        epochMilliseconds
    ) {
        if (epochMilliseconds == null) {
            value = false
            return@produceState
        }

        val remainingMilliseconds = epochMilliseconds - Clock.System.now().toEpochMilliseconds()
        value = remainingMilliseconds <= 0
        delay(remainingMilliseconds.milliseconds)
        value = true
    }

    return hasPassed
}
