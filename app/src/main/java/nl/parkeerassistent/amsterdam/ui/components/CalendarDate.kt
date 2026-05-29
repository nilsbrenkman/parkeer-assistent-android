package nl.parkeerassistent.amsterdam.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.AppType
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Small day-of-week + day-of-month stack (iOS `CalendarDate`). */
@Composable
fun CalendarDate(date: LocalDate, modifier: Modifier = Modifier) {
    Column(modifier = modifier.width(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
            style = AppType.calendarDow,
            color = AppTheme.colors.danger,
        )
        Text(text = date.dayOfMonth.toString(), style = AppType.calendarDay)
    }
}
