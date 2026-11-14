package com.eeseka.lynk.core.presentation.util

// Commas every three digits, e.g. 10000 reads "10,000"
fun Long.toGroupedString(): String {
    return toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
}
