package com.eeseka.lynk.hangouts.presentation.hangout_detail.memories.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Images
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.hangouts.presentation.hangout_album.components.HangoutPhotoThumbnail
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.DetailSection
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.PlaceholderCard
import com.eeseka.lynk.hangouts.presentation.model.HangoutPhotoUi
import com.eeseka.lynk.hangouts.presentation.preview.previewHangoutPhotos
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.components.util.shimmerEffect
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.HangoutConstants.MAX_PHOTOS_PER_UPLOADER
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.detail_see_all
import lynk.feature.hangouts.generated.resources.memories_add_photos
import lynk.feature.hangouts.generated.resources.memories_adding_photos
import lynk.feature.hangouts.generated.resources.memories_empty_message
import lynk.feature.hangouts.generated.resources.memories_empty_title
import lynk.feature.hangouts.generated.resources.memories_limit_reached
import lynk.feature.hangouts.generated.resources.memories_title
import lynk.feature.hangouts.generated.resources.memories_title_with_count
import org.jetbrains.compose.resources.stringResource

private const val PLACEHOLDER_THUMBNAIL_COUNT = 5

@Composable
fun MemoriesSection(
    photos: ImmutableList<HangoutPhotoUi>,
    photoCount: Int,
    remainingPhotoSlots: Int,
    isLoadingPhotos: Boolean,
    isUploadingPhotos: Boolean,
    onPhotoClick: (String) -> Unit,
    onPhotoLoadFailed: () -> Unit,
    onSeeAllClick: () -> Unit,
    onAddPhotosClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()
    val showEmptyAlbum = photos.isEmpty() && !isLoadingPhotos
    val showPlaceholders = photos.isEmpty() && isLoadingPhotos

    DetailSection(
        title = if (photoCount > 0) stringResource(Res.string.memories_title_with_count, photoCount)
        else stringResource(Res.string.memories_title),
        trailing = if (photos.isNotEmpty()) {
            {
                Row(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onSeeAllClick()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LynkText(
                        text = stringResource(Res.string.detail_see_all),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Icon(
                        imageVector = Lucide.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else null,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (photos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items = photos, key = { it.id }) { photo ->
                        HangoutPhotoThumbnail(
                            photoId = photo.id,
                            photoThumbnailUrl = photo.thumbnailUrl,
                            photoCaption = photo.caption,
                            onClick = { onPhotoClick(photo.id) },
                            onLoadFailed = onPhotoLoadFailed,
                            modifier = Modifier.size(96.dp)
                        )
                    }
                }
            }

            if (showPlaceholders) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false
                ) {
                    items(PLACEHOLDER_THUMBNAIL_COUNT) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .shimmerEffect()
                        )
                    }
                }
            }

            if (showEmptyAlbum) {
                PlaceholderCard(
                    icon = Lucide.Images,
                    title = stringResource(Res.string.memories_empty_title),
                    message = stringResource(Res.string.memories_empty_message),
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LynkButton(
                text = stringResource(Res.string.memories_add_photos),
                onClick = {
                    hapticFeedback(AppHaptic.ImpactLight)
                    onAddPhotosClick()
                },
                style = LynkButtonStyle.SECONDARY,
                enabled = remainingPhotoSlots > 0,
                isLoading = isUploadingPhotos,
                loadingText = stringResource(Res.string.memories_adding_photos),
                modifier = Modifier.padding(top = 8.dp)
            )

            if (remainingPhotoSlots == 0 && !isLoadingPhotos) {
                LynkText(
                    text = stringResource(
                        Res.string.memories_limit_reached,
                        MAX_PHOTOS_PER_UPLOADER
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MemoriesSectionPhotosPreview() = MemoriesSectionPreview(photos = previewHangoutPhotos)

@PreviewLightDark
@Composable
private fun MemoriesSectionEmptyPreview() = MemoriesSectionPreview()

@PreviewLightDark
@Composable
private fun MemoriesSectionLimitReachedPreview() = MemoriesSectionPreview(
    photos = previewHangoutPhotos,
    remainingPhotoSlots = 0
)

@Composable
private fun MemoriesSectionPreview(
    photos: ImmutableList<HangoutPhotoUi> = persistentListOf(),
    remainingPhotoSlots: Int = MAX_PHOTOS_PER_UPLOADER - photos.size
) {
    LynkTheme {
        MemoriesSection(
            photos = photos,
            photoCount = photos.size,
            remainingPhotoSlots = remainingPhotoSlots,
            isLoadingPhotos = false,
            isUploadingPhotos = false,
            onPhotoClick = {},
            onPhotoLoadFailed = {},
            onSeeAllClick = {},
            onAddPhotosClick = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)
        )
    }
}
