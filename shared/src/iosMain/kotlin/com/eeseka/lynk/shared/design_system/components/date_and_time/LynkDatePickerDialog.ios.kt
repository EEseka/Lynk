package com.eeseka.lynk.shared.design_system.components.date_and_time

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import com.eeseka.lynk.shared.presentation.util.toPickerDate
import com.eeseka.lynk.shared.presentation.util.toPickerMillis
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents
import platform.UIKit.UICalendarSelectionSingleDate
import platform.UIKit.UICalendarSelectionSingleDateDelegateProtocol
import platform.UIKit.UICalendarView
import platform.darwin.NSObject

@Composable
actual fun LynkDatePickerDialog(
    onDateSelected: (Long) -> Unit,
    onDismissRequest: () -> Unit,
    initialSelectedDateMillis: Long?
) {
    val viewController = LocalUIViewController.current
    val tintColor = MaterialTheme.colorScheme.primary
    val currentOnDateSelected by rememberUpdatedState(onDateSelected)
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)

    DisposableEffect(viewController) {
        val selectionDelegate = CalendarSelectionDelegate { date -> currentOnDateSelected(date.toPickerMillis()) }
        // Apple's calendar can start with no day selected, so the first tap on any day, today included, picks it
        val selection = UICalendarSelectionSingleDate(delegate = selectionDelegate)
        val calendarView = UICalendarView()
        calendarView.calendar = NSCalendar.currentCalendar
        calendarView.selectionBehavior = selection

        val initialDate = initialSelectedDateMillis?.toPickerDate()
        if (initialDate != null) {
            val initialComponents = initialDate.toDateComponents()
            selection.setSelectedDate(initialComponents, animated = false)
            calendarView.visibleDateComponents = initialComponents
        }

        val popover = presentPickerPopover(
            host = viewController,
            pickerView = calendarView,
            tintColor = tintColor,
            onDismissedByUser = { currentOnDismissRequest() },
            retainedObjects = listOf(selectionDelegate, selection)
        )

        onDispose { popover.close() }
    }
}

private class CalendarSelectionDelegate(
    private val onDatePicked: (LocalDate) -> Unit
) : NSObject(), UICalendarSelectionSingleDateDelegateProtocol {

    override fun dateSelection(selection: UICalendarSelectionSingleDate, didSelectDate: NSDateComponents?) {
        val components = didSelectDate ?: return
        onDatePicked(LocalDate(components.year.toInt(), components.month.toInt(), components.day.toInt()))
    }
}

private fun LocalDate.toDateComponents(): NSDateComponents {
    val components = NSDateComponents()
    components.setYear(year.toLong())
    components.setMonth(month.number.toLong())
    components.setDay(day.toLong())
    return components
}
