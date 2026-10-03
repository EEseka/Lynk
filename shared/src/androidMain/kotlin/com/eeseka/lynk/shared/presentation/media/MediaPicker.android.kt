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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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

    val multipleGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(MAX_PICKABLE_IMAGES)
    ) { uris -> mediaPicker.onPickImagesResult(uris) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> mediaPicker.onCaptureImageResult(success) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> mediaPicker.onPermissionResult(isGranted) }

    mediaPicker.registerLaunchers(
        galleryLauncher = { galleryLauncher.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        multipleGalleryLauncher = { maxCount ->
            multipleGalleryLauncher.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly, maxItems = maxCount))
        },
        cameraLauncher = { uri -> cameraLauncher.launch(uri) },
        permissionLauncher = { permissionLauncher.launch(Manifest.permission.CAMERA) }
    )

    return mediaPicker
}

// The most the multiple picker can ever allow; each launch can only lower it
private const val MAX_PICKABLE_IMAGES = 10

private class MediaPickerAndroid(private val context: Context) : MediaPicker {
    private var galleryLauncher: (() -> Unit)? = null
    private var multipleGalleryLauncher: ((Int) -> Unit)? = null
    private var cameraLauncher: ((Uri) -> Unit)? = null
    private var permissionLauncher: (() -> Unit)? = null

    private var activeCameraContinuation: Continuation<PickedImage?>? = null
    private var activeUriContinuation: Continuation<Uri?>? = null
    private var activeUrisContinuation: Continuation<List<Uri>>? = null
    private var tempImageUri: Uri? = null

    fun registerLaunchers(
        galleryLauncher: () -> Unit,
        multipleGalleryLauncher: (Int) -> Unit,
        cameraLauncher: (Uri) -> Unit,
        permissionLauncher: () -> Unit
    ) {
        this.galleryLauncher = galleryLauncher
        this.multipleGalleryLauncher = multipleGalleryLauncher
        this.cameraLauncher = cameraLauncher
        this.permissionLauncher = permissionLauncher
    }

    override suspend fun pickImage(): PickedImage? {
        val uri = suspendCancellableCoroutine<Uri?> { cont ->
            activeUriContinuation = cont
            galleryLauncher?.invoke()
        } ?: return null

        return withContext(Dispatchers.IO) {
            uri.toPickedImage()
        }
    }

    override suspend fun pickImages(maxCount: Int): List<PickedImage> {
        if (maxCount < 1) return emptyList()
        if (maxCount == 1) return listOfNotNull(pickImage())

        val uris = suspendCancellableCoroutine { cont ->
            activeUrisContinuation = cont
            multipleGalleryLauncher?.invoke(maxCount)
        }

        // Copied off the main thread, since a handful of full photos is a lot of bytes
        return withContext(Dispatchers.IO) {
            // Old phones without the photo picker fall back to a file chooser that ignores the limit
            uris.take(maxCount)
                .map { uri -> async { uri.toPickedImage() } }
                .awaitAll()
                .filterNotNull()
        }
    }

    override suspend fun captureImage(): PickedImage? {
        return suspendCancellableCoroutine { cont ->
            activeCameraContinuation = cont
            permissionLauncher?.invoke()
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            val uri = createTempCacheUri()
            if (uri != null) {
                tempImageUri = uri
                cameraLauncher?.invoke(uri)
            } else {
                activeCameraContinuation?.resume(null)
                activeCameraContinuation = null
            }
        } else {
            activeCameraContinuation?.resume(null)
            activeCameraContinuation = null
        }
    }

    fun onCaptureImageResult(success: Boolean) {
        if (success && tempImageUri != null) {
            val filePath = tempImageUri.toString()
            activeCameraContinuation?.resume(PickedImage(filePath, "image/jpeg"))
        } else {
            activeCameraContinuation?.resume(null)
        }
        activeCameraContinuation = null
        tempImageUri = null
    }

    fun onPickImageResult(uri: Uri?) {
        activeUriContinuation?.resume(uri)
        activeUriContinuation = null
    }

    fun onPickImagesResult(uris: List<Uri>) {
        activeUrisContinuation?.resume(uris)
        activeUrisContinuation = null
    }

    private fun Uri.toPickedImage(): PickedImage? {
        val mimeType = context.contentResolver.getType(this) ?: "image/jpeg"
        return copyUriToCache(context, this)?.let { file ->
            PickedImage(Uri.fromFile(file).toString(), mimeType)
        }
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

            inputStream.use { input ->
                file.outputStream().use { outputStream ->
                    input.copyTo(outputStream)
                }
            }
            file
        } catch (_: Exception) {
            null
        }
    }
}