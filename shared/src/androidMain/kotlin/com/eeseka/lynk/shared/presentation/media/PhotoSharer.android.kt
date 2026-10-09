package com.eeseka.lynk.shared.presentation.media

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@Composable
actual fun rememberPhotoSharer(): PhotoSharer {
    val context = LocalContext.current
    return remember { PhotoSharerAndroid(context) }
}

private class PhotoSharerAndroid(private val context: Context) : PhotoSharer {

    override suspend fun share(photos: List<CaptionedPhoto>): Boolean {
        val files = withContext(Dispatchers.IO) {
            try {
                val directory = File(context.cacheDir, "shared_images")
                if (!directory.exists()) directory.mkdirs()
                photos.map { photo ->
                    val photoBytes = photo.caption?.let { withCaption(context, photo.imageBytes, it) } ?: photo.imageBytes
                    File(directory, "share_${UUID.randomUUID()}.jpg").apply { writeBytes(photoBytes) }
                }
            } catch (_: Exception) {
                null
            }
        } ?: return false
        if (files.isEmpty()) return false

        val uris = files.map { file ->
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
        val sendIntent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, uris.single())
                photos.single().caption?.let { putExtra(Intent.EXTRA_TEXT, it) }
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            }
        }.apply {
            type = "image/jpeg"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(chooser)
            true
        } catch (_: Exception) {
            false
        }
    }
}
