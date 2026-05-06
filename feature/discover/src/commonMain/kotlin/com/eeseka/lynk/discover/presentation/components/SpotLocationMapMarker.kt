package com.eeseka.lynk.discover.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.spot.mappers.getIcon
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.ClickResult

@Composable
fun SpotLocationMapMarker(
    spots: List<Spot>,
    selectedSpotId: String?,
    onSpotClick: (String) -> Unit
) {
    val unselectedSpots = spots.filter { it.id != selectedSpotId }
    val selectedSpot = spots.find { it.id == selectedSpotId }

    RenderSpotLayers(
        spots = unselectedSpots,
        layerPrefix = "unselected",
        scale = 1f,
        offsetY = 0f,
        onSpotClick = onSpotClick
    )

    if (selectedSpot != null) {
        AnimatedSelectedSpotLayer(
            spot = selectedSpot,
            onSpotClick = onSpotClick
        )
    }
}

@Composable
private fun AnimatedSelectedSpotLayer(
    spot: Spot,
    onSpotClick: (String) -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1.35f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "spot_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "spot_bob")
    val bobOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spot_bob_y"
    )

    RenderSpotLayers(
        spots = listOf(spot),
        layerPrefix = "selected",
        scale = animatedScale,
        offsetY = bobOffsetY,
        onSpotClick = onSpotClick
    )
}

@Composable
private fun RenderSpotLayers(
    spots: List<Spot>,
    layerPrefix: String,
    scale: Float,
    offsetY: Float,
    onSpotClick: (String) -> Unit
) {
    SpotCategory.entries.forEach { category ->
        val matchingSpots = spots.filter { it.category == category }

        if (matchingSpots.isNotEmpty()) {
            val spotsGeoJson = remember(matchingSpots) {
                val features = matchingSpots.joinToString(",") { spot ->
                    """{ "type": "Feature", "geometry": { "type": "Point", "coordinates": [${spot.longitude}, ${spot.latitude}] }, "properties": { "id": "${spot.id}" } }"""
                }
                """{ "type": "FeatureCollection", "features": [$features] }"""
            }

            val source = rememberGeoJsonSource(data = GeoJsonData.JsonString(spotsGeoJson))
            val pinPainter = rememberCategoryPinPainter(category)

            SymbolLayer(
                id = "spots-$layerPrefix-${category.name.lowercase()}",
                source = source,
                iconImage = image(pinPainter),
                iconAnchor = const(SymbolAnchor.Bottom),
                iconAllowOverlap = const(true),
                iconSize = const(scale),
                iconOffset = const(DpOffset(0.dp, offsetY.dp)),
                onClick = { features ->
                    val clickedFeature = features.firstOrNull()
                    val clickedSpotId =
                        clickedFeature?.properties?.get("id")?.toString()?.replace("\"", "")
                    if (clickedSpotId != null) onSpotClick(clickedSpotId)
                    ClickResult.Consume
                }
            )
        }
    }
}

@Composable
private fun rememberCategoryPinPainter(category: SpotCategory): Painter {
    val coreColor = getCategoryPinColor(category)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val iconPainter = rememberVectorPainter(image = category.getIcon())

    val density = LocalDensity.current

    return remember(category, coreColor, surfaceColor, outlineColor, iconPainter, density) {
        object : Painter() {
            val widthPx = with(density) { 36.dp.toPx() }
            val heightPx = with(density) { 50.dp.toPx() }
            val strokePx = with(density) { 2.dp.toPx() }

            override val intrinsicSize: Size = Size(widthPx, heightPx)

            override fun DrawScope.onDraw() {
                val width = size.width
                val height = size.height
                val radius = width / 2f

                val path = Path().apply {
                    moveTo(radius, height)
                    quadraticTo(0f, height * 0.65f, 0f, radius)
                    arcTo(Rect(0f, 0f, width, width), 180f, 180f, false)
                    quadraticTo(width, height * 0.65f, radius, height)
                    close()
                }

                drawPath(path, color = outlineColor, style = Stroke(width = strokePx))
                drawPath(path, color = surfaceColor)
                drawCircle(
                    color = coreColor,
                    radius = radius * 0.75f,
                    center = Offset(radius, radius)
                )

                val iconSize = width * 0.45f
                translate(left = (width - iconSize) / 2f, top = (width - iconSize) / 2f) {
                    with(iconPainter) {
                        draw(Size(iconSize, iconSize), colorFilter = ColorFilter.tint(Color.White))
                    }
                }
            }
        }
    }
}

private fun getCategoryPinColor(category: SpotCategory): Color {
    return when (category) {
        SpotCategory.RESTAURANT -> Color(0xFFE63946)
        SpotCategory.CAFE -> Color(0xFFF59E0B)
        SpotCategory.LOUNGE -> Color(0xFF8B5CF6)
        SpotCategory.CLUB -> Color(0xFFD946EF)
        SpotCategory.ACTIVITY -> Color(0xFF10B981)
        SpotCategory.OTHER -> Color(0xFF64748B)
    }
}