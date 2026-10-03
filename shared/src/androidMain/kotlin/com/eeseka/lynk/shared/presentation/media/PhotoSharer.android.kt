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

    override suspend fun share(imageBytes: ByteArray, caption: String?): Boolean {
        val file = withContext(Dispatchers.IO) {
            try {
                val directory = File(context.cacheDir, "shared_images")
                if (!directory.exists()) directory.mkdirs()
                File(directory, "share_${UUID.randomUUID()}.jpg").apply { writeBytes(imageBytes) }
            } catch (_: Exception) {
                null
            }
        } ?: return false

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            caption?.let { putExtra(Intent.EXTRA_TEXT, it) }
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
