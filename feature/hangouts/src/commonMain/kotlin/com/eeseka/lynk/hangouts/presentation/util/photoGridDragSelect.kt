package com.eeseka.lynk.hangouts.presentation.util

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toIntRect

// Drag across the grid to pick every photo between, the way Photos does: long-press first anywhere,
// or once Select is on, just swipe sideways (an up or down swipe still scrolls the grid).
// isSelecting is read when each touch starts, so turning Select on never restarts a drag already going.
// Near the top or bottom edge the grid scrolls on its own, faster the closer the finger gets
fun Modifier.photoGridDragSelect(
    gridState: LazyGridState,
    isSelecting: () -> Boolean,
    onDragStart: (photoId: String) -> Unit,
    onDragOver: (photoId: String) -> Unit,
    onDragEnd: () -> Unit,
    onAutoScrollSpeedChange: (pixelsPerFrame: Float) -> Unit
): Modifier = pointerInput(gridState) {
    val edgeSize = 64.dp.toPx()

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val dragStart = if (isSelecting()) awaitSwipeOrLongPress(down.id) else awaitLongPressOrCancellation(down.id)
        val startPhotoId = dragStart?.let { gridState.photoIdAt(down.position) } ?: return@awaitEachGesture

        var lastPhotoId = startPhotoId
        onDragStart(startPhotoId)

        fun moveTo(position: Offset) {
            // The edges sit where the photos begin and end, not under the top bar or the home indicator
            val layoutInfo = gridState.layoutInfo
            val distanceFromTop = position.y - layoutInfo.beforeContentPadding
            val distanceFromBottom = size.height - layoutInfo.afterContentPadding - position.y
            onAutoScrollSpeedChange(
                when {
                    distanceFromBottom < edgeSize -> edgeSize - distanceFromBottom
                    distanceFromTop < edgeSize -> -(edgeSize - distanceFromTop)
                    else -> 0f
                } / 4f
            )

            gridState.photoIdAt(position)?.let { photoId ->
                if (photoId != lastPhotoId) {
                    lastPhotoId = photoId
                    onDragOver(photoId)
                }
            }
        }

        moveTo(dragStart.position)
        drag(dragStart.id) { change ->
            moveTo(change.position)
            change.consume()
        }
        onAutoScrollSpeedChange(0f)
        onDragEnd()
    }
}

// Whichever comes first: a sideways swipe, or the finger held still for a long-press.
// Null when the finger lifts, or an up or down move hands the touch to the grid's scroll
private suspend fun AwaitPointerEventScope.awaitSwipeOrLongPress(pointerId: PointerId): PointerInputChange? {
    var swipe: PointerInputChange? = null
    var isCancelled = false
    withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
        swipe = awaitHorizontalTouchSlopOrCancellation(pointerId) { change, _ -> change.consume() }
        isCancelled = swipe == null
    }
    if (swipe != null || isCancelled) return swipe

    // Held still past the long-press time
    return currentEvent.changes.firstOrNull { it.id == pointerId && it.pressed }
}

// Only photo cells are keyed by their id, so the loading row and empty state never match.
// Cell offsets start below the grid's top content padding (viewportStartOffset is minus that padding),
// while the finger is measured from the grid's top edge, so the padding is taken off first
private fun LazyGridState.photoIdAt(offset: Offset): String? {
    val touchInContent = offset.round() + IntOffset(x = 0, y = layoutInfo.viewportStartOffset)
    return layoutInfo.visibleItemsInfo
        .find { item -> item.size.toIntRect().contains(touchInContent - item.offset) }
        ?.key as? String
}
