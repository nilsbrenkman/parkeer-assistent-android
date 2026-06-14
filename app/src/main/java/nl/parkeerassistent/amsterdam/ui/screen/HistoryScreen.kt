package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.data.model.Parking
import nl.parkeerassistent.amsterdam.ui.components.CalendarDate
import nl.parkeerassistent.amsterdam.ui.components.LicensePlate
import nl.parkeerassistent.amsterdam.ui.components.SectionHeader
import nl.parkeerassistent.amsterdam.ui.components.TitleBar
import nl.parkeerassistent.amsterdam.ui.parking.ParkingViewModel
import nl.parkeerassistent.amsterdam.ui.theme.AppShape
import nl.parkeerassistent.amsterdam.ui.theme.AppType
import nl.parkeerassistent.amsterdam.ui.theme.Dimens
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import nl.parkeerassistent.amsterdam.util.DateUtil
import nl.parkeerassistent.amsterdam.util.LicenseUtil
import nl.parkeerassistent.amsterdam.util.VisitorNameCache
import java.time.LocalDate
import java.time.YearMonth

/** Parking history, grouped by month (iOS `HistoryListView`). */
@Composable
fun HistoryListScreen(
    parkingVm: ParkingViewModel,
    onOpen: (parkingId: Long) -> Unit,
) {
    val history by parkingVm.history.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { parkingVm.getHistory() }
    HistoryListContent(history = history, onOpen = onOpen)
}

@Composable
internal fun HistoryListContent(history: List<Parking>?, onOpen: (parkingId: Long) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TitleBar(title = stringResource(R.string.parking_history))
        when {
            history == null -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            history.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(stringResource(R.string.parking_no_history)) }
            else -> {
                val groups = history.groupBy { YearMonth.from(DateUtil.toLocalDate(it.startTime) ?: LocalDate.now()) }
                LazyColumn(
                    contentPadding = Dimens.contentPadding,
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    groups.forEach { (month, rows) ->
                        item(key = "h-$month") {
                            SectionHeader(
                                DateUtil.formatMonthYear(month.atDay(1)),
                                Modifier.padding(bottom = Dimens.paddingMini),
                            )
                        }
                        items(rows, key = { it.id }) { row ->
                            HistoryRow(row, onClick = { onOpen(row.id) })
                        }
                        item { Spacer(Modifier.height(Dimens.spacingLarge)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(parking: Parking, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface, AppShape.roundedSmall)
            .padding(all = Dimens.paddingMini),
    ) {
        DateUtil.toLocalDate(parking.startTime)?.let { CalendarDate(it) }
        LicensePlate(LicenseUtil.format(parking.license))
        Text(VisitorNameCache.map[parking.license] ?: "", style = AppType.name)
    }
}

private val sampleHistory = listOf(
    Parking(id = 1, license = "12ABC3", name = "Jan", startTime = "2026-05-29T14:00:00+02:00", endTime = "2026-05-29T15:00:00+02:00", cost = 1.20),
    Parking(id = 2, license = "55ABC6", name = "Erik", startTime = "2026-04-12T10:00:00+02:00", endTime = "2026-04-12T11:30:00+02:00", cost = 2.10),
)

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun HistoryListPreview() = ParkeerAssistentTheme { HistoryListContent(sampleHistory) {} }
