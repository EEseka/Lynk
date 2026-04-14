package com.eeseka.lynk.shared.presentation.media

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.eeseka.lynk.shared.domain.media.model.PickedImage
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.UUID
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

@Composable
actual fun rememberMediaPicker(): MediaPicker {
    val context = LocalContext.current
    val mediaPicker = remember { MediaPickerAndroid(context) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> mediaPicker.onPickImageResult(uri) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> mediaPicker.onCaptureImageResult(success) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> mediaPicker.onPermissionResult(isGranted) }

    mediaPicker.registerLaunchers(
        galleryLauncher = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        cameraLauncher = { uri -> cameraLauncher.launch(uri) },
        permissionLauncher = { permissionLauncher.launch(Manifest.permission.CAMERA) }
    )

    return mediaPicker
}

class MediaPickerAndroid(private val context: Context) : MediaPicker {
    private var galleryLauncher: (() -> Unit)? = null
    private var cameraLauncher: ((Uri) -> Unit)? = null
    private var permissionLauncher: (() -> Unit)? = null

    private var activeContinuation: Continuation<PickedImage?>? = null
    private var tempImageUri: Uri? = null

    fun registerLaunchers(
        galleryLauncher: () -> Unit,
        cameraLauncher: (Uri) -> Unit,
        permissionLauncher: () -> Unit
    ) {
        this.galleryLauncher = galleryLauncher
        this.cameraLauncher = cameraLauncher
        this.permissionLauncher = permissionLauncher
    }

    override suspend fun pickImage(): PickedImage? {
        return suspendCancellableCoroutine { cont ->
            activeContinuation = cont
            galleryLauncher?.invoke()
        }
    }

    override suspend fun captureImage(): PickedImage? {
        return suspendCancellableCoroutine { cont ->
            activeContinuation = cont
            permissionLauncher?.invoke()
        }
    }

    // --- Internal Logic ---

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            val uri = createTempCacheUri()
            if (uri != null) {
                tempImageUri = uri
                cameraLauncher?.invoke(uri)
            } else {
                activeContinuation?.resume(null)
                activeContinuation = null
            }
        } else {
            activeContinuation?.resume(null)
            activeContinuation = null
        }
    }

    fun onCaptureImageResult(success: Boolean) {
        if (success && tempImageUri != null) {
            val filePath = tempImageUri.toString()
            activeContinuation?.resume(PickedImage(filePath, "image/jpeg"))
        } else {
            activeContinuation?.resume(null)
        }
        activeContinuation = null
        tempImageUri = null
    }

    fun onPickImageResult(uri: Uri?) {
        if (uri != null) {
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val cachedFile = copyUriToCache(context, uri)
            if (cachedFile != null) {
                val fileUri = Uri.fromFile(cachedFile).toString()
                activeContinuation?.resume(PickedImage(fileUri, mimeType))
            } else {
                activeContinuation?.resume(null)
            }
        } else {
            activeContinuation?.resume(null)
        }
        activeContinuation = null
    }

    private fun createTempCacheUri(): Uri? {
        return try {
            val directory = File(context.cacheDir, "shared_images")
            if (!directory.exists()) directory.mkdirs()
            val file = File.createTempFile("camera_", ".jpg", directory)
            val authority = "${context.packageName}.fileprovider"
            FileProvider.getUriForFile(context, authority, file)
        } catch (_: Exception) {
            null
        }
    }

    private fun copyUriToCache(context: Context, sourceUri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val directory = File(context.cacheDir, "picked_images")
            if (!directory.exists()) directory.mkdirs()

            val fileName = "picked_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)

            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file
        } catch (_: Exception) {
            null
        }
    }
}