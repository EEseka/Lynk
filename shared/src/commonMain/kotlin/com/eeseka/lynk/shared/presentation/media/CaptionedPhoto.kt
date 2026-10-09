package com.eeseka.lynk.shared.presentation.media

// A photo on its way out of the app, saved or shared, with the caption that goes into the file
class CaptionedPhoto(
    val imageBytes: ByteArray,
    val caption: String?
)
