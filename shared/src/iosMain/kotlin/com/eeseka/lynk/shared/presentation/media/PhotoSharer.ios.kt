package com.eeseka.lynk.shared.presentation.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.popoverPresentationController

@Composable
actual fun rememberPhotoSharer(): PhotoSharer {
    return remember { PhotoSharerIos() }
}

private class PhotoSharerIos : PhotoSharer {

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun share(photos: List<CaptionedPhoto>): Boolean {
        // A UIImage drops the caption tag, so each photo is shared as its own file
        val photoFiles = photos.mapNotNull { photo -> photo.toTaggedFile() }
        if (photoFiles.isEmpty()) return false
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return false

        val captionText = photos.singleOrNull()?.caption
        val controller = UIActivityViewController(
            activityItems = photoFiles + listOfNotNull(captionText),
            applicationActivities = null
        )
        controller.popoverPresentationController?.apply {
            sourceView = rootViewController.view
            sourceRect = rootViewController.view.bounds.useContents {
                CGRectMake(x = size.width / 2, y = size.height / 2, width = 0.0, height = 0.0)
            }
            permittedArrowDirections = 0uL
        }

        rootViewController.presentViewController(controller, animated = true, completion = null)
        return true
    }

    private fun CaptionedPhoto.toTaggedFile(): NSURL? {
        val photoData = imageBytes.toNSData()
        val taggedData = caption?.let { withCaption(photoData, it) } ?: photoData
        val fileUrl = NSURL.fileURLWithPath("${NSTemporaryDirectory()}share_${NSUUID().UUIDString}.jpg")
        return fileUrl.takeIf { taggedData.writeToURL(it, atomically = true) }
    }
}
