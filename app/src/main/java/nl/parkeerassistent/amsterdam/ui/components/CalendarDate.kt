package nl.parkeerassistent.amsterdam.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.AppType
import java.time.LocalDate
import java.time.format.TextStyle

/** Small day-of-week + day-of-month stack (iOS `CalendarDate`). */
@Composable
fun CalendarDate(date: LocalDate, modifier: Modifier = Modifier) {
    val locale = LocalLocale.current.platformLocale
    Column(modifier = modifier.width(42.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).uppercase(),
            style = AppType.calendarDow,
            color = AppTheme.colors.danger,
        )
        Text(text = date.dayOfMonth.toString(), style = AppType.calendarDay)
    }
}
