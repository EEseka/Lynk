package com.eeseka.lynk.assistant.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos

private const val MIN_CHARS_PER_SECOND = 45f
private const val MAX_CHARS_PER_SECOND = 600f
private const val CATCH_UP_PER_SECOND = 4f

/**
 * Words arrive from the server in uneven bursts; this shows them at a steady pace,
 * timed by the clock so a 120 Hz screen types no faster than a 60 Hz one
 **/
@Composable
fun rememberRevealedText(text: String): String {
    var shownLength by remember { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        var lastFrameNanos = withFrameNanos { it }
        var pendingChars = 0f
        while (shownLength < text.length) {
            val frameNanos = withFrameNanos { it }
            val seconds = (frameNanos - lastFrameNanos) / 1_000_000_000f
            lastFrameNanos = frameNanos

            // Speeds up gently as the backlog grows, so a burst eases in instead of landing at once
            val backlog = text.length - shownLength
            val charsPerSecond = (backlog * CATCH_UP_PER_SECOND).coerceIn(MIN_CHARS_PER_SECOND, MAX_CHARS_PER_SECOND)
            pendingChars += charsPerSecond * seconds

            val step = pendingChars.toInt()
            if (step > 0) {
                var nextLength = (shownLength + step).coerceAtMost(text.length)
                // Never stops between the two halves of an emoji, which would show a broken box for a frame
                if (nextLength < text.length && text[nextLength - 1].isHighSurrogate()) nextLength++
                shownLength = nextLength
                pendingChars -= step
            }
        }
        if (shownLength > text.length) shownLength = text.length
    }

    return text.take(shownLength)
}
