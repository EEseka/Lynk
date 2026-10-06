package com.eeseka.lynk.discover.presentation.util

import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import org.maplibre.spatialk.geojson.BoundingBox

// The smallest box holding every spot, for fitting the camera to a list; null for no spots
fun List<SpotUi>.toBoundingBox(): BoundingBox? {
    if (isEmpty()) return null

    return BoundingBox(
        west = minOf { it.longitude },
        south = minOf { it.latitude },
        east = maxOf { it.longitude },
        north = maxOf { it.latitude }
    )
}
