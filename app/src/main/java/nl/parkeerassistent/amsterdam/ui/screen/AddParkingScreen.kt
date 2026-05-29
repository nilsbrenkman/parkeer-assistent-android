package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.data.model.Visitor
import nl.parkeerassistent.amsterdam.ui.components.DataBox
import nl.parkeerassistent.amsterdam.ui.components.RegimeDatePickerDialog
import nl.parkeerassistent.amsterdam.ui.components.TitleBar
import nl.parkeerassistent.amsterdam.ui.components.VisitorView
import nl.parkeerassistent.amsterdam.ui.components.WheelSelector
import nl.parkeerassistent.amsterdam.ui.parking.ParkingViewModel
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.AppType
import nl.parkeerassistent.amsterdam.ui.theme.Dimens
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import nl.parkeerassistent.amsterdam.ui.user.UserViewModel
import nl.parkeerassistent.amsterdam.util.DateUtil
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.ceil

/**
 * Start a parking session (iOS `AddParkingView`). Wheel adjusts minutes (or the start time when
 * the start-time box is active); times are clamped to today/now and the day's paid regime window.
 * The map-based meter picker is Phase 7 — the default `parkingMeterId` from `getUser` is used.
 */
@Composable
fun AddParkingScreen(
    visitor: Visitor,
    userVm: UserViewModel,
    parkingVm: ParkingViewModel,
    onBack: () -> Unit,
    onPickMeter: () -> Unit,
) {
    val user by userVm.state.collectAsStateWithLifecycle()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var startDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    var minutes by remember { mutableIntStateOf(0) }
    var customStartDate by remember { mutableStateOf(false) }
    var modifyStartTime by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var wait by remember { mutableStateOf(false) }

    var startTimeStr by remember { mutableStateOf("") }
    var endTimeStr by remember { mutableStateOf("") }
    var costStr by remember { mutableStateOf("0.00") }

    fun regimeStartDT() = selectedDate.atTime(user.regimeTimeStart ?: LocalTime.now())
    fun regimeEndDT() = selectedDate.atTime(user.regimeTimeEnd ?: LocalTime.now())
    fun isToday() = selectedDate == LocalDate.now()

    fun minimumStartTime(): LocalDateTime {
        val now = LocalDateTime.now()
        return when {
            !isToday() -> regimeStartDT()
            regimeStartDT().isAfter(now) -> regimeStartDT()
            regimeEndDT().isBefore(now) -> regimeEndDT()
            else -> now
        }
    }

    fun applyMinutes(value: Int) {
        val end = startDateTime.plusMinutes(value.toLong())
        minutes = if (end.isAfter(regimeEndDT())) {
            val secs = Duration.between(startDateTime, regimeEndDT()).seconds
            ceil(secs / 60.0).toInt().coerceAtLeast(0)
        } else {
            value
        }
        costStr = DateUtil.calculateCost(minutes, user.hourRate)
    }

    fun applyStartDate(start: LocalDateTime) {
        when {
            start.isAfter(regimeEndDT()) -> startDateTime = regimeEndDT()
            start.isAfter(minimumStartTime()) -> {
                startDateTime = start
                customStartDate = true
            }
            else -> {
                startDateTime = minimumStartTime()
                customStartDate = false
            }
        }
    }

    fun update() {
        applyStartDate(if (customStartDate) startDateTime else minimumStartTime())
        startTimeStr = DateUtil.formatTime(startDateTime)
        applyMinutes(minutes)
        endTimeStr = DateUtil.formatTime(startDateTime.plusMinutes(minutes.toLong()))
    }

    fun onWheel(diff: Int) {
        if (modifyStartTime) {
            customStartDate = true
            startDateTime = startDateTime.plusMinutes(diff.toLong())
            update()
            return
        }
        val next = minutes + diff
        when {
            next < 0 -> minutes = 0
            next > user.timeBalance -> minutes = user.timeBalance
            else -> applyMinutes(next)
        }
        update()
    }

    LaunchedEffect(selectedDate, user.regimeTimeStart, user.regimeTimeEnd) { update() }

    AddParkingContent(
        visitor = visitor,
        dateText = DateUtil.formatDayMonth(selectedDate),
        startTimeText = startTimeStr,
        endTimeText = endTimeStr,
        minutesText = minutes.toString(),
        costText = costStr,
        meterId = user.parkingMeterId,
        dateActive = showDatePicker,
        startTimeActive = modifyStartTime,
        addEnabled = minutes > 0 && user.parkingMeterId != null,
        onDateClick = { showDatePicker = true },
        onStartTimeClick = { modifyStartTime = true },
        onMinutesClick = { modifyStartTime = false },
        onPickMeter = onPickMeter,
        onWheel = ::onWheel,
        onAdd = {
            if (!wait && minutes > 0) {
                wait = true
                parkingVm.startParking(
                    visitor = visitor,
                    timeMinutes = minutes,
                    start = startDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime(),
                    productId = user.productId ?: 0,
                    zoneId = user.zoneId ?: 0,
                    parkingMeterId = user.parkingMeterId ?: 0,
                ) {
                    userVm.getBalance()
                    onBack()
                }
                wait = false
            }
        },
    )

    if (showDatePicker) {
        RegimeDatePickerDialog(
            regime = user.regime,
            initialDate = selectedDate,
            onDismiss = { showDatePicker = false },
            onSelect = { date ->
                selectedDate = date
                userVm.getRegime(date)
            },
        )
    }
}

@Composable
private fun AddParkingContent(
    visitor: Visitor,
    dateText: String,
    startTimeText: String,
    endTimeText: String,
    minutesText: String,
    costText: String,
    meterId: Long?,
    dateActive: Boolean,
    startTimeActive: Boolean,
    addEnabled: Boolean,
    onDateClick: () -> Unit,
    onStartTimeClick: () -> Unit,
    onMinutesClick: () -> Unit,
    onPickMeter: () -> Unit,
    onWheel: (Int) -> Unit,
    onAdd: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TitleBar(title = stringResource(R.string.parking_header))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingNormal),
        ) {
            VisitorView(visitor)

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spacingNormal)) {
                DataBox(
                    title = stringResource(R.string.parking_date),
                    content = dateText,
                    modifier = Modifier.weight(1f).alpha(if (dateActive) 1f else 0.5f).clickable(onClick = onDateClick),
                )
                DataBox(
                    title = stringResource(R.string.parking_start_time),
                    content = startTimeText,
                    modifier = Modifier.weight(1f).alpha(if (startTimeActive) 1f else 0.5f).clickable(onClick = onStartTimeClick),
                )
                SignBox(meterId = meterId, modifier = Modifier.weight(1f).clickable(onClick = onPickMeter))
            }

            Row(
                modifier = Modifier.alpha(if (!startTimeActive) 1f else 0.5f).clickable(onClick = onMinutesClick),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingNormal),
            ) {
                DataBox(stringResource(R.string.parking_minutes), minutesText, Modifier.weight(1f))
                DataBox(stringResource(R.string.parking_end_time), endTimeText, Modifier.weight(1f))
                DataBox(stringResource(R.string.parking_cost), "€ $costText", Modifier.weight(1f))
            }

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                WheelSelector(radius = 75.dp, thickness = 16.dp, onChange = onWheel)
            }

            Button(
                onClick = onAdd,
                enabled = addEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.success,
                    contentColor = AppTheme.colors.enabled,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.common_add)) }
        }
    }
}

@Composable
private fun SignBox(meterId: Long?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Dimens.spacingXSmall)) {
        Text("${stringResource(R.string.parking_sign)}:", style = AppType.dataBoxTitle)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(Dimens.radiusSmall))
                .background(AppTheme.colors.header),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = meterId?.toString() ?: "...",
                style = AppType.dataBoxContent,
                fontWeight = FontWeight.Bold,
                color = if (meterId != null) Color.White else AppTheme.colors.danger,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun AddParkingPreview() = ParkeerAssistentTheme {
    AddParkingContent(
        visitor = Visitor(1, "22BBB2", "22-BBB-2", "Erik"),
        dateText = "29 mei",
        startTimeText = "17:00",
        endTimeText = "17:14",
        minutesText = "13",
        costText = "0.51",
        meterId = 55105,
        dateActive = false,
        startTimeActive = false,
        addEnabled = true,
        onDateClick = {},
        onStartTimeClick = {},
        onMinutesClick = {},
        onPickMeter = {},
        onWheel = {},
        onAdd = {},
    )
}
