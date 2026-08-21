package com.eeseka.lynk.profile.presentation.saved_spots.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Star
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.spot.mappers.getTitle
import com.eeseka.lynk.shared.presentation.spot.util.SpotPhotoUrlBuilder
import com.eeseka.lynk.shared.presentation.spot.util.getPriceLevelSymbol
import lynk.feature.profile.generated.resources.Res
import lynk.feature.profile.generated.resources.save_spot
import lynk.feature.profile.generated.resources.unsave_spot
import org.jetbrains.compose.resources.stringResource

@Composable
fun SavedSpotListItem(
    spotName: String,
    spotPhotos: List<String>,
    spotAddress: String?,
    spotCategory: SpotCategory,
    spotPriceLevel: PriceLevel?,
    spotRating: Double?,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryPhotoUrl = remember(spotPhotos) {
        SpotPhotoUrlBuilder.getPrimaryPhotoUrl(spotPhotos)
    }

    LynkCard(
        style = LynkCardStyle.FILLED,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (primaryPhotoUrl != null) {
                    AsyncImage(
                        model = primaryPhotoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Lucide.MapPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LynkText(
                    text = spotName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val metadata = buildString {
                    append(spotCategory.getTitle())
                    if (spotPriceLevel != null) {
                        append(" • ")
                        append(getPriceLevelSymbol(spotPriceLevel.tier))
                    }
                }

                LynkText(
                    text = metadata,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                spotAddress?.let {
                    LynkText(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                spotRating?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Lucide.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.extended.gold
                        )
                        LynkText(
                            text = it.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = if (isSaved) Icons.Filled.Bookmark
                else Icons.Outlined.BookmarkBorder,
                contentDescription = stringResource(
                    if (isSaved) Res.string.unsave_spot else Res.string.save_spot
                ),
                tint = if (isSaved) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable(onClick = onToggleSave)
                    .padding(8.dp)
                    .size(20.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SavedSpotListItemPreview() {
    LynkTheme {
        SavedSpotListItem(
            spotName = "Nok by Alara",
            spotPhotos = emptyList(),
            spotAddress = "Victoria Island, Lagos",
            spotCategory = SpotCategory.RESTAURANT,
            spotPriceLevel = PriceLevel.MODERATE,
            spotRating = 4.6,
            isSaved = true,
            onClick = {},
            onToggleSave = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
}