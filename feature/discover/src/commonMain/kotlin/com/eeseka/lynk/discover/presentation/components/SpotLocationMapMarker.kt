package com.eeseka.lynk.discover.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.spot.mappers.getIcon
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.condition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MapState
import org.maplibre.compose.overlay.MapOverlayScope
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.spatialk.geojson.Position

private const val SELECTED_PIN_SCALE = 1.35f
private val BOB_TRAVEL = (-12).dp

private val PIN_WIDTH = 36.dp
private val PIN_HEIGHT = 48.dp

// No spot id is ever blank, so this stands in for "nothing selected" in the filter expression.
private const val NO_SELECTION_ID = ""

private val SPOT_LAYER_IDS: Set<String> = SpotCategory.entries.map { it.layerId() }.toSet()

@Composable
fun rememberSpotMapInteractions(
    mapState: MapState,
    onSpotClick: (String) -> Unit
): MapInteractions {
    val scope = rememberCoroutineScope()

    return remember(mapState, onSpotClick) {
        MapInteractions {
            callbacks {
                click {
                    onEvent { event ->
                        scope.launch {
                            val features = mapState.queryRenderedFeatures(
                                offset = event.screenOffset,
                                layerIds = SPOT_LAYER_IDS
                            )
                            features.firstNotNullOfOrNull { feature ->
                                (feature.properties?.get("id") as? JsonPrimitive)?.content
                            }?.let(onSpotClick)
                        }
                        ClickResult.Consume
                    }
                }
            }
        }
    }
}

@Composable
fun SpotLocationMapMarker(
    spots: ImmutableList<SpotUi>,
    selectedSpotId: String?
) {
    val geoJsonByCategory = remember(spots) {
        SpotCategory.entries.associateWith { category ->
            spots.filter { it.category == category }.toFeatureCollectionJson()
        }
    }

    val selectedCategory = remember(spots, selectedSpotId) {
        spots.find { it.id == selectedSpotId }?.category
    }

    SpotCategory.entries.forEach { category ->
        SpotCategoryLayer(
            category = category,
            geoJson = geoJsonByCategory.getValue(category),
            selectedSpotId = selectedSpotId.takeIf { category == selectedCategory }
        )
    }
}

@Composable
fun MapOverlayScope.SelectedSpotPinOverlay(spot: SpotUi?) {
    if (spot == null) return

    val painter = rememberCategoryPinPainter(spot.category)

    val transition = rememberInfiniteTransition(label = "spot_bob")
    val bobFraction = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spot_bob_y"
    )

    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .placedAt(
                position = Position(longitude = spot.longitude, latitude = spot.latitude),
                alignment = Alignment.BottomCenter
            )
            .size(
                width = PIN_WIDTH * SELECTED_PIN_SCALE,
                height = PIN_HEIGHT * SELECTED_PIN_SCALE
            )
            .graphicsLayer {
                translationY = BOB_TRAVEL.toPx() * bobFraction.value
            }
    )
}

@Composable
private fun SpotCategoryLayer(
    category: SpotCategory,
    geoJson: String,
    selectedSpotId: String?
) {
    val source = rememberGeoJsonSource(data = GeoJsonData.JsonString(geoJson))
    val pinPainter = rememberCategoryPinPainter(category)

    val isSelected = feature["id"].asString() eq const(selectedSpotId ?: NO_SELECTION_ID)

    SymbolLayer(
        id = category.layerId(),
        source = source,
        iconImage = image(pinPainter),
        iconAnchor = const(SymbolAnchor.Bottom),
        iconAllowOverlap = const(true),
        iconOpacity = switch(
            condition(isSelected, const(0f)),
            fallback = const(1f)
        )
    )
}

@Composable
private fun rememberCategoryPinPainter(category: SpotCategory): Painter {
    val coreColor = getCategoryPinColor(category)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val iconPainter = rememberVectorPainter(image = category.getIcon())

    val density = LocalDensity.current

    return remember(category, surfaceColor, outlineColor, density) {
        object : Painter() {
            val widthPx = with(density) { PIN_WIDTH.toPx() }
            val heightPx = with(density) { PIN_HEIGHT.toPx() }
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

private fun SpotCategory.layerId(): String = "spots-${name.lowercase()}"

private fun List<SpotUi>.toFeatureCollectionJson(): String {
    val features = joinToString(",") { spot ->
        """{ "type": "Feature", "geometry": { "type": "Point", "coordinates": [${spot.longitude}, ${spot.latitude}] }, "properties": { "id": "${spot.id}" } }"""
    }
    return """{ "type": "FeatureCollection", "features": [$features] }"""
}
