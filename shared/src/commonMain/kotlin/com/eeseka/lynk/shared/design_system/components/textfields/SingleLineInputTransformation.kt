package com.eeseka.lynk.shared.design_system.components.textfields

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer

// Text meant for one line, like a name or a short note: anything but a line break
private val SINGLE_LINE = Regex("[^\\r\\n\\u0085\\u2028\\u2029]*")

object SingleLineInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        if (asCharSequence().matches(SINGLE_LINE)) return
        for (index in length - 1 downTo 0) {
            if (!charAt(index).toString().matches(SINGLE_LINE)) replace(index, index + 1, " ")
        }
    }
}
