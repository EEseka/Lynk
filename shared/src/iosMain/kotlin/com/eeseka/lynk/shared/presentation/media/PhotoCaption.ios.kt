package com.eeseka.lynk.shared.presentation.media

import cnames.structs.__CFData
import cnames.structs.__CFString
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.Foundation.create
import platform.ImageIO.CGImageDestinationCopyImageSource
import platform.ImageIO.CGImageDestinationCreateWithData
import platform.ImageIO.CGImageMetadataCreateMutable
import platform.ImageIO.CGImageMetadataSetValueMatchingImageProperty
import platform.ImageIO.CGImageSourceCreateWithData
import platform.ImageIO.CGImageSourceGetType
import platform.ImageIO.kCGImageDestinationMergeMetadata
import platform.ImageIO.kCGImageDestinationMetadata
import platform.ImageIO.kCGImagePropertyIPTCCaptionAbstract
import platform.ImageIO.kCGImagePropertyIPTCDictionary

// Photos reads the caption from the IPTC caption, which ImageIO also mirrors into the XMP description.
// Copying the source adds the metadata without re-encoding the pixels.
@OptIn(ExperimentalForeignApi::class)
internal fun withCaption(photoData: NSData, caption: String): NSData? {
    val cfPhotoData = CFBridgingRetain(photoData)?.reinterpret<__CFData>() ?: return null
    val taggedData = NSMutableData()
    val cfTaggedData = CFBridgingRetain(taggedData)?.reinterpret<__CFData>()
    val cfCaption = CFBridgingRetain(caption)?.reinterpret<__CFString>()
    val metadata = CGImageMetadataCreateMutable()
    val options = CFDictionaryCreateMutable(
        kCFAllocatorDefault,
        2L,
        kCFTypeDictionaryKeyCallBacks.ptr,
        kCFTypeDictionaryValueCallBacks.ptr
    )

    try {
        if (cfTaggedData == null || cfCaption == null || metadata == null || options == null) return null

        val source = CGImageSourceCreateWithData(cfPhotoData, null) ?: return null
        try {
            val imageType = CGImageSourceGetType(source) ?: return null
            val destination = CGImageDestinationCreateWithData(cfTaggedData, imageType, 1uL, null)
                ?: return null
            try {
                val isCaptionSet = CGImageMetadataSetValueMatchingImageProperty(
                    metadata,
                    kCGImagePropertyIPTCDictionary,
                    kCGImagePropertyIPTCCaptionAbstract,
                    cfCaption
                )
                if (!isCaptionSet) return null

                CFDictionarySetValue(options, kCGImageDestinationMetadata, metadata)
                CFDictionarySetValue(options, kCGImageDestinationMergeMetadata, kCFBooleanTrue)

                val isCopied = CGImageDestinationCopyImageSource(destination, source, options, null)
                return if (isCopied) taggedData else null
            } finally {
                CFRelease(destination)
            }
        } finally {
            CFRelease(source)
        }
    } finally {
        options?.let { CFRelease(it) }
        metadata?.let { CFRelease(it) }
        cfCaption?.let { CFRelease(it) }
        cfTaggedData?.let { CFRelease(it) }
        CFRelease(cfPhotoData)
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
}
