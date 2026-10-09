package com.eeseka.lynk.shared.presentation.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAssetCreationRequest
import platform.Photos.PHAssetResourceTypePhoto
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import kotlin.coroutines.resume

@Composable
actual fun rememberPhotoSaver(): PhotoSaver {
    return remember { PhotoSaverIos() }
}

private class PhotoSaverIos : PhotoSaver {

    override suspend fun saveToGallery(photo: CaptionedPhoto): Boolean {
        val status = suspendCancellableCoroutine { cont ->
            PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { status ->
                cont.resume(status)
            }
        }
        if (status != PHAuthorizationStatusAuthorized && status != PHAuthorizationStatusLimited) return false

        val photoData = photo.imageBytes.toNSData()
        val imageData = photo.caption?.let { withCaption(photoData, it) } ?: photoData

        return suspendCancellableCoroutine { cont ->
            PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                changeBlock = {
                    PHAssetCreationRequest.creationRequestForAsset().addResourceWithType(
                        type = PHAssetResourceTypePhoto,
                        data = imageData,
                        options = null
                    )
                },
                completionHandler = { isSaved, _ -> cont.resume(isSaved) }
            )
        }
    }
}
