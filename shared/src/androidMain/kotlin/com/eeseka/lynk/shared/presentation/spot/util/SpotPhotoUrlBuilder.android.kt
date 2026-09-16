package com.eeseka.lynk.shared.presentation.spot.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import coil3.PlatformContext
import com.eeseka.lynk.AppConfig
import java.security.MessageDigest

actual fun getGooglePlacesApiKey() = AppConfig.GOOGLE_PLACES_ANDROID_API_KEY

actual fun getGoogleApiHeaders(context: PlatformContext): Map<String, String> {
    return mapOf(
        "X-Android-Package" to "com.eeseka.lynk",
        "X-Android-Cert" to context.signingCertificateSha1()
    )
}

// Read from the key that signed this install, so debug, release and Play builds each send their own
@Suppress("DEPRECATION")
private fun Context.signingCertificateSha1(): String {
    val signature = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            .signingInfo?.apkContentsSigners?.firstOrNull()
    } else {
        packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            .signatures?.firstOrNull()
    } ?: return ""

    // Google only accepts the fingerprint without colons: AB269ADF..., not AB:26:9A:...
    return MessageDigest.getInstance("SHA-1")
        .digest(signature.toByteArray())
        .joinToString("") { byte -> "%02X".format(byte) }
}
