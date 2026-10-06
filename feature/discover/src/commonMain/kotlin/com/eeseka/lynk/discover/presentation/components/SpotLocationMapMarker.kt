package com.eeseka.lynk.discover.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
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
import org.maplibre.compose.expressions.dsl.format
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.span
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.expressions.dsl.textOffset
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
    selectedSpotId: String?,
    isRanked: Boolean
) {
    val geoJsonByCategory = remember(spots, isRanked) {
        val rankById = if (isRanked) spots.mapIndexed { index, spot -> spot.id to index + 1 }.toMap() else emptyMap()
        SpotCategory.entries.associateWith { category ->
            spots.filter { it.category == category }.toFeatureCollectionJson(rankById)
        }
    }

    val selectedCategory = remember(spots, selectedSpotId) {
        spots.find { it.id == selectedSpotId }?.category
    }

    SpotCategory.entries.forEach { category ->
        SpotCategoryLayer(
            category = category,
            geoJson = geoJsonByCategory.getValue(category),
            selectedSpotId = selectedSpotId.takeIf { category == selectedCategory },
            isRanked = isRanked
        )
    }
}

@Composable
fun MapOverlayScope.SelectedSpotPinOverlay(spot: SpotUi?, rank: Int? = null) {
    if (spot == null) return

    val painter = rememberCategoryPinPainter(spot.category, showIcon = rank == null)

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

    Box(
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
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.matchParentSize()
        )
        rank?.let { number ->
            // The pin's round head, where the category icon usually sits
            Box(
                modifier = Modifier.size(PIN_WIDTH * SELECTED_PIN_SCALE),
                contentAlignment = Alignment.Center
            ) {
                LynkText(
                    text = number.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SpotCategoryLayer(
    category: SpotCategory,
    geoJson: String,
    selectedSpotId: String?,
    isRanked: Boolean
) {
    val source = rememberGeoJsonSource(data = GeoJsonData.JsonString(geoJson))
    val pinPainter = rememberCategoryPinPainter(category, showIcon = !isRanked)

    val isSelected = feature["id"].asString() eq const(selectedSpotId ?: NO_SELECTION_ID)
    val selectedHidden = switch(
        condition(isSelected, const(0f)),
        fallback = const(1f)
    )

    SymbolLayer(
        id = category.layerId(),
        source = source,
        iconImage = image(pinPainter),
        iconAnchor = const(SymbolAnchor.Bottom),
        iconAllowOverlap = const(true),
        iconOpacity = selectedHidden,
        // Blank on an unranked pin, so the normal map draws no text
        textField = format(span(feature["rank"].asString())),
        // A font both MapTiler styles ship, light and dark
        textFont = const(listOf("Roboto Bold", "Noto Sans Bold")),
        textColor = const(Color.White),
        textSize = const(14.sp),
        // Up from the pin's tip to the center of its round head
        textOffset = textOffset(0.dp, -(PIN_HEIGHT - PIN_WIDTH / 2)),
        textAllowOverlap = const(true),
        textOpacity = selectedHidden
    )
}

@Composable
private fun rememberCategoryPinPainter(category: SpotCategory, showIcon: Boolean = true): Painter {
    val coreColor = getCategoryPinColor(category)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val iconPainter = rememberVectorPainter(image = category.getIcon())

    val density = LocalDensity.current

    return remember(category, showIcon, surfaceColor, outlineColor, density) {
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

                if (!showIcon) return

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

// Every pin carries a rank, blank when it has none, so the layer's text always has a string to read
private fun List<SpotUi>.toFeatureCollectionJson(rankById: Map<String, Int>): String {
    val features = joinToString(",") { spot ->
        val rank = rankById[spot.id]?.toString().orEmpty()
        """{ "type": "Feature", "geometry": { "type": "Point", "coordinates": [${spot.longitude}, ${spot.latitude}] }, "properties": { "id": "${spot.id}", "rank": "$rank" } }"""
    }
    return """{ "type": "FeatureCollection", "features": [$features] }"""
}
