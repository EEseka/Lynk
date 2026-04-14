package com.eeseka.lynk.shared.presentation.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.eeseka.lynk.shared.domain.media.model.PickedImage
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import kotlin.coroutines.resume

@Composable
actual fun rememberMediaPicker(): MediaPicker {
    return remember { MediaPickerIos() }
}

class MediaPickerIos : MediaPicker {
    private var strongPickerDelegate: NSObject? = null
    private var strongCameraDelegate: NSObject? = null

    override suspend fun pickImage(): PickedImage? {
        return suspendCancellableCoroutine { cont ->
            val controller = PHPickerViewController(PHPickerConfiguration().apply {
                selectionLimit = 1
                filter = PHPickerFilter.imagesFilter
            })

            val delegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
                override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                    // Clear the strong ref to allow cleanup
                    strongPickerDelegate = null

                    picker.dismissViewControllerAnimated(true, null)

                    val result = didFinishPicking.firstOrNull() as? PHPickerResult
                    if (result != null) {
                        result.itemProvider.loadFileRepresentationForTypeIdentifier("public.image") { url, _ ->
                            if (url is NSURL) {
                                // Copy to temp to ensure we own the file
                                val newPath = copyToTemp(url)
                                if (newPath != null) {
                                    cont.resume(PickedImage(newPath, "image/jpeg"))
                                } else {
                                    cont.resume(null)
                                }
                            } else {
                                cont.resume(null)
                            }
                        }
                    } else {
                        cont.resume(null)
                    }
                }
            }

            // Assign to strong reference BEFORE assigning to controller
            strongPickerDelegate = delegate
            controller.delegate = delegate

            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                controller,
                true,
                null
            )

            cont.invokeOnCancellation {
                controller.dismissViewControllerAnimated(true, null)
                strongPickerDelegate = null
            }
        }
    }

    override suspend fun captureImage(): PickedImage? {
        val isAuthorized = checkCameraPermission()
        if (!isAuthorized) return null

        return suspendCancellableCoroutine { cont ->
            val controller = UIImagePickerController()
            controller.sourceType =
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera

            val delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol,
                UINavigationControllerDelegateProtocol {
                override fun imagePickerController(
                    picker: UIImagePickerController,
                    didFinishPickingMediaWithInfo: Map<Any?, *>
                ) {
                    strongCameraDelegate = null
                    picker.dismissViewControllerAnimated(true, null)

                    val image =
                        didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage

                    if (image != null) {
                        val data = UIImageJPEGRepresentation(image, 0.8)
                        val path = saveDetailsToTemp(data)
                        if (path != null) {
                            cont.resume(PickedImage(path, "image/jpeg"))
                        } else {
                            cont.resume(null)
                        }
                    } else {
                        cont.resume(null)
                    }
                }

                override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                    strongCameraDelegate = null
                    picker.dismissViewControllerAnimated(true, null)
                    cont.resume(null)
                }
            }

            strongCameraDelegate = delegate
            controller.delegate = delegate

            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                controller,
                true,
                null
            )

            cont.invokeOnCancellation {
                controller.dismissViewControllerAnimated(true, null)
                strongCameraDelegate = null
            }
        }
    }

    private suspend fun checkCameraPermission(): Boolean {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        if (status == AVAuthorizationStatusAuthorized) return true

        return suspendCancellableCoroutine { cont ->
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                cont.resume(granted)
            }
        }
    }

    private fun copyToTemp(originalUrl: NSURL): String? {
        val data = NSData.dataWithContentsOfURL(originalUrl)
        return saveDetailsToTemp(data)
    }

    private fun saveDetailsToTemp(data: NSData?): String? {
        if (data == null) return null
        val fileName = NSUUID.UUID().UUIDString + ".jpg"
        val url =
            NSURL.fileURLWithPath(NSTemporaryDirectory()).URLByAppendingPathComponent(fileName)
                ?: return null
        data.writeToURL(url, true)
        return url.absoluteString
    }
}