package com.eeseka.lynk.shared.presentation.media

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

@Composable
actual fun rememberPhotoSaver(): PhotoSaver {
    val context = LocalContext.current
    val photoSaver = remember { PhotoSaverAndroid(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> photoSaver.onPermissionResult(isGranted) }

    photoSaver.registerPermissionLauncher {
        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    return photoSaver
}

private class PhotoSaverAndroid(private val context: Context) : PhotoSaver {

    private companion object {
        const val ALBUM_NAME = "Lynk"
    }

    private var permissionLauncher: (() -> Unit)? = null
    private var activeContinuation: Continuation<Boolean>? = null

    fun registerPermissionLauncher(permissionLauncher: () -> Unit) {
        this.permissionLauncher = permissionLauncher
    }

    override suspend fun saveToGallery(photo: CaptionedPhoto): Boolean {
        // Android 10 and later write to the shared Pictures folder without asking
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !hasWritePermission()) {
            val isGranted = suspendCancellableCoroutine { cont ->
                activeContinuation = cont
                permissionLauncher?.invoke()
            }
            if (!isGranted) return false
        }

        return withContext(Dispatchers.IO) {
            try {
                // Without a caption, or when tagging fails, the photo saves as it came
                val photoBytes = photo.caption?.let { withCaption(context, photo.imageBytes, it) } ?: photo.imageBytes

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    insertIntoMediaStore(photoBytes)
                } else {
                    writeToPicturesFolder(photoBytes)
                }
            } catch (_: Exception) {
                false
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        activeContinuation?.resume(isGranted)
        activeContinuation = null
    }

    private fun hasWritePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun insertIntoMediaStore(imageBytes: ByteArray): Boolean {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, newFileName())
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM_NAME")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values) ?: return false
        val isWritten = try {
            resolver.openOutputStream(uri)?.use { it.write(imageBytes) } != null
        } catch (_: Exception) {
            false
        }
        if (!isWritten) {
            resolver.delete(uri, null, null)
            return false
        }

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return true
    }

    private fun writeToPicturesFolder(imageBytes: ByteArray): Boolean {
        val picturesFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val albumFolder = File(picturesFolder, ALBUM_NAME)
        if (!albumFolder.exists() && !albumFolder.mkdirs()) return false

        val file = File(albumFolder, newFileName())
        file.writeBytes(imageBytes)

        // The gallery only lists files the media scanner has seen
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/jpeg"), null)
        return true
    }

    private fun newFileName(): String = "lynk_${System.currentTimeMillis()}.jpg"
}
