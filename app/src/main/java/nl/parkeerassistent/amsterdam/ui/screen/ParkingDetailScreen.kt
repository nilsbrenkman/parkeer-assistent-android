package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.data.model.Parking
import nl.parkeerassistent.amsterdam.ui.components.DangerButton
import nl.parkeerassistent.amsterdam.ui.components.LicensePlate
import nl.parkeerassistent.amsterdam.ui.components.Property
import nl.parkeerassistent.amsterdam.ui.components.TitleBar
import nl.parkeerassistent.amsterdam.ui.parking.ParkingViewModel
import nl.parkeerassistent.amsterdam.ui.theme.AppShape
import nl.parkeerassistent.amsterdam.ui.theme.Dimens
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import nl.parkeerassistent.amsterdam.ui.visitor.VisitorViewModel
import nl.parkeerassistent.amsterdam.util.DateUtil
import nl.parkeerassistent.amsterdam.util.LicenseUtil

/**
 * Active/scheduled parking detail with a stop action (iOS `ParkingDetailView`). Mirrors
 * [HistoryDetailScreen] but resolves the session from the live parking list and adds a destructive
 * "Stop session" button. Stopping pops back to the home screen (the list refreshes from the VM).
 */
@Composable
fun ParkingDetailScreen(
    parkingId: Long,
    parkingVm: ParkingViewModel,
    visitorVm: VisitorViewModel,
    onBack: () -> Unit,
) {
    val parkingResponse by parkingVm.parking.collectAsStateWithLifecycle()
    val parking = parkingResponse?.let { r ->
        (r.active + r.scheduled).firstOrNull { it.id == parkingId }
    }
    ParkingDetailContent(
        parking = parking,
        name = parking?.let { visitorVm.getName(it.license) } ?: "",
        onStop = {
            parking?.let { parkingVm.stopParking(it) }
            onBack()
        },
    )
}

@Composable
internal fun ParkingDetailContent(parking: Parking?, name: String, onStop: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TitleBar(title = stringResource(R.string.parking_details))
        if (parking == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { Text(stringResource(R.string.parking_no_history)) }
            return@Column
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.contentPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            Spacer(Modifier.height(Dimens.spacingSmall))

            Box(Modifier.fillMaxWidth().padding(vertical = Dimens.paddingSmall), Alignment.Center) {
                LicensePlate(LicenseUtil.format(parking.license))
            }

            Spacer(Modifier.height(Dimens.spacingSmall))

            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, AppShape.roundedSmall)
                    .padding(horizontal = Dimens.spacingNormal, vertical = Dimens.spacingSmall),
            ) {
                Property(stringResource(R.string.visitor_name), name)
                HorizontalDivider()
                Property(stringResource(R.string.parking_cost), "€ ${"%.2f".format(parking.cost)}")
                HorizontalDivider()
                Property(stringResource(R.string.parking_start_time), DateUtil.formatParking(parking.startTime))
                HorizontalDivider()
                Property(stringResource(R.string.parking_end_time), DateUtil.formatParking(parking.endTime))
            }

            DangerButton(onClick = onStop) { Text(stringResource(R.string.parking_stop)) }
        }
    }
}

@Preview(showBackground = true, heightDp = 500)
@Composable
private fun ParkingDetailPreview() = ParkeerAssistentTheme {
    ParkingDetailContent(
        parking = Parking(id = 1, license = "12ABC3", name = "Jan", startTime = "2026-05-29T14:00:00+02:00", endTime = "2026-05-29T15:00:00+02:00", cost = 1.20),
        name = "Jan Jansen",
        onStop = {},
    )
}
