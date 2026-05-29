package nl.parkeerassistent.amsterdam.ui.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import nl.parkeerassistent.amsterdam.data.model.Regime
import nl.parkeerassistent.amsterdam.util.DateUtil
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Material 3 replacement for the iOS `UICalendarView` + `DatePickerModal` + selection delegate.
 * Only today-or-later dates whose weekday has a paid [regime] entry are selectable (the iOS
 * `canSelectDate` rule). The picker works in UTC millis, matching `DatePickerState`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegimeDatePickerDialog(
    regime: Regime?,
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onSelect: (LocalDate) -> Unit,
) {
    val today = LocalDate.now()

    val selectableDates = remember(regime, today) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                if (date.isBefore(today)) return false
                val r = regime ?: return false
                return DateUtil.getRegimeDay(r, date) != null
            }

            override fun isSelectableYear(year: Int): Boolean = year >= today.year
        }
    }

    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = selectableDates,
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let {
                    onSelect(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuleer") }
        },
    ) {
        DatePicker(state = state)
    }
}
