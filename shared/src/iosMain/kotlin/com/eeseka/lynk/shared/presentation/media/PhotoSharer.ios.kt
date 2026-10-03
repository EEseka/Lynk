package com.eeseka.lynk.shared.presentation.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.popoverPresentationController

@Composable
actual fun rememberPhotoSharer(): PhotoSharer {
    return remember { PhotoSharerIos() }
}

private class PhotoSharerIos : PhotoSharer {

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun share(imageBytes: ByteArray, caption: String?): Boolean {
        val image = UIImage.imageWithData(imageBytes.toNSData()) ?: return false
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return false

        val controller = UIActivityViewController(
            activityItems = listOfNotNull(image, caption),
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
}
