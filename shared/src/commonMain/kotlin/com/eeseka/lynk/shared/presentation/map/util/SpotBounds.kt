package com.eeseka.lynk.shared.presentation.map.util

import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position

// The smallest box around every spot and the extra point, so the camera can fit them all on screen; null when there's nothing to fit in the screen
fun List<SpotUi>.toBoundingBox(including: Position? = null): BoundingBox? {
    if (isEmpty() && including == null) return null

    val longitudes = map { it.longitude } + listOfNotNull(including?.longitude)
    val latitudes = map { it.latitude } + listOfNotNull(including?.latitude)

    return BoundingBox(
        west = longitudes.min(),
        south = latitudes.min(),
        east = longitudes.max(),
        north = latitudes.max()
    )
}
