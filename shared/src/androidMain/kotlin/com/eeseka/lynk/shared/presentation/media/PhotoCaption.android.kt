package com.eeseka.lynk.shared.presentation.media

import android.content.Context
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.util.UUID

// Gallery apps read a photo's caption from its XMP description, which ExifInterface only writes to a file
internal fun withCaption(context: Context, imageBytes: ByteArray, caption: String): ByteArray? {
    val file = File(context.cacheDir, "caption_${UUID.randomUUID()}.jpg")
    return try {
        file.writeBytes(imageBytes)
        ExifInterface(file).apply {
            setAttribute(ExifInterface.TAG_XMP, captionXmp(caption))
            saveAttributes()
        }
        file.readBytes()
    } catch (_: Exception) {
        null
    } finally {
        file.delete()
    }
}

private fun captionXmp(caption: String): String {
    val escapedCaption = caption
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    return """<x:xmpmeta xmlns:x="adobe:ns:meta/">""" +
            """<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">""" +
            """<rdf:Description rdf:about="" xmlns:dc="http://purl.org/dc/elements/1.1/">""" +
            """<dc:description><rdf:Alt><rdf:li xml:lang="x-default">$escapedCaption</rdf:li></rdf:Alt></dc:description>""" +
            """</rdf:Description></rdf:RDF></x:xmpmeta>"""
}
