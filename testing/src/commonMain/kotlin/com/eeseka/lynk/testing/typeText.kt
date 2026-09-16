package com.eeseka.lynk.testing

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshots.Snapshot

// A running screen tells snapshotFlow about new text every frame; a ViewModel test has no frames, so send that signal here
fun TextFieldState.typeText(text: String) {
    setTextAndPlaceCursorAtEnd(text)
    Snapshot.sendApplyNotifications()
}
