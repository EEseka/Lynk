package com.eeseka.lynk.shared.presentation.map.util

import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position

fun List<SpotUi>.toBoundingBox(): BoundingBox? =
    boundingBoxOf(map { Position(longitude = it.longitude, latitude = it.latitude) })

// The smallest box around every position, so the camera can fit them all on screen; null when there's nothing to fit in the screen
fun boundingBoxOf(positions: List<Position>): BoundingBox? {
    if (positions.isEmpty()) return null

    return BoundingBox(
        west = positions.minOf { it.longitude },
        south = positions.minOf { it.latitude },
        east = positions.maxOf { it.longitude },
        north = positions.maxOf { it.latitude }
    )
}
