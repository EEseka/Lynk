package com.eeseka.lynk.shared.design_system.components.date_and_time

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSDate
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import platform.UIKit.UIDatePickerStyle
import platform.darwin.NSObject
import platform.objc.sel_registerName

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LynkTimePickerDialog(
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismissRequest: () -> Unit,
    initialHour: Int?,
    initialMinute: Int?
) {
    val viewController = LocalUIViewController.current
    val tintColor = MaterialTheme.colorScheme.primary
    val currentOnTimeSelected by rememberUpdatedState(onTimeSelected)
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)

    DisposableEffect(viewController) {
        val picker = UIDatePicker()
        picker.datePickerMode = UIDatePickerMode.UIDatePickerModeTime
        picker.preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleWheels

        if (initialHour != null) {
            val initialTime = NSCalendar.currentCalendar.dateBySettingHour(
                h = initialHour.toLong(),
                minute = (initialMinute ?: 0).toLong(),
                second = 0,
                ofDate = NSDate(),
                options = 0uL
            )
            if (initialTime != null) picker.date = initialTime
        }

        // The wheels apply as they turn, like iOS; closing keeps the time unless nothing was set or turned
        var hasTurnedWheels = false
        val wheelsTarget = ValueChangedTarget { hasTurnedWheels = true }
        picker.addTarget(wheelsTarget, action = sel_registerName("onValueChanged"), forControlEvents = UIControlEventValueChanged)

        val popover = presentPickerPopover(
            host = viewController,
            pickerView = picker,
            tintColor = tintColor,
            onDismissedByUser = {
                if (hasTurnedWheels || initialHour != null) {
                    val time = NSCalendar.currentCalendar.components(NSCalendarUnitHour or NSCalendarUnitMinute, fromDate = picker.date)
                    currentOnTimeSelected(time.hour.toInt(), time.minute.toInt())
                } else {
                    currentOnDismissRequest()
                }
            },
            retainedObjects = listOf(wheelsTarget)
        )

        onDispose { popover.close() }
    }
}

@OptIn(BetaInteropApi::class)
private class ValueChangedTarget(
    private val onValueChanged: () -> Unit
) : NSObject() {

    // UIKit calls it by name through the selector, which the IDE can't see
    @Suppress("unused")
    @ObjCAction
    fun onValueChanged() {
        onValueChanged.invoke()
    }
}
