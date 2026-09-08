package com.eeseka.lynk.shared.presentation.util

import io.ktor.http.encodeURLParameter

fun buildMailtoUri(
    email: String,
    subject: String,
    body: String
): String = "mailto:$email" +
        "?subject=${subject.encodeURLParameter()}" +
        "&body=${body.encodeURLParameter()}"
