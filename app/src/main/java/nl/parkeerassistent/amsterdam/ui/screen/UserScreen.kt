package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.data.model.Parking
import nl.parkeerassistent.amsterdam.data.model.ParkingResponse
import nl.parkeerassistent.amsterdam.data.model.Visitor
import nl.parkeerassistent.amsterdam.review.AppReview
import nl.parkeerassistent.amsterdam.stats.StatsStore
import nl.parkeerassistent.amsterdam.ui.common.MessageBus
import nl.parkeerassistent.amsterdam.ui.common.MessageType
import nl.parkeerassistent.amsterdam.ui.components.LicensePlate
import nl.parkeerassistent.amsterdam.ui.components.SectionHeader
import nl.parkeerassistent.amsterdam.ui.parking.ParkingViewModel
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.AppType
import nl.parkeerassistent.amsterdam.ui.theme.Dimens
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import nl.parkeerassistent.amsterdam.ui.user.UserViewModel
import nl.parkeerassistent.amsterdam.ui.visitor.VisitorViewModel
import nl.parkeerassistent.amsterdam.util.VisitorNameCache
import nl.parkeerassistent.amsterdam.util.findActivity
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/** iOS caps saved visitors at 9. */
private const val MAX_VISITORS = 9

/** Callbacks the home screen needs (groups the menu/navigation handlers). */
class UserActions(
    val onLogout: () -> Unit,
    val onInfo: () -> Unit,
    val onHistory: () -> Unit,
    val onPayment: () -> Unit,
    val onAccounts: () -> Unit,
    val onSettings: () -> Unit,
    val onAddVisitor: () -> Unit,
    val onAddParking: (visitorId: Long) -> Unit,
    val onDeleteVisitor: (Visitor) -> Unit,
    val onTooManyVisitors: () -> Unit,
    val onBalanceTap: () -> Unit,
    val onStop: (Parking) -> Unit,
)

/**
 * Home screen (iOS `UserView` = `ParkingView` + `VisitorListView`). Loads user/visitor/parking on
 * first show and refreshes periodically.
 */
@Composable
fun UserScreen(
    onLogout: () -> Unit,
    onInfo: () -> Unit,
    onHistory: () -> Unit,
    onPayment: () -> Unit,
    onAccounts: () -> Unit,
    onSettings: () -> Unit,
    onAddVisitor: () -> Unit,
    onAddParking: (visitorId: Long) -> Unit,
    userVm: UserViewModel = koinViewModel(),
    visitorVm: VisitorViewModel = koinViewModel(),
    parkingVm: ParkingViewModel = koinViewModel(),
    statsStore: StatsStore = koinInject(),
    appReview: AppReview = koinInject(),
    messageBus: MessageBus = koinInject(),
) {
    val user by userVm.state.collectAsStateWithLifecycle()
    val visitors by visitorVm.visitors.collectAsStateWithLifecycle()
    val parking by parkingVm.parking.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val tooManyMsg = stringResource(R.string.visitor_too_many_msg)

    LaunchedEffect(Unit) {
        if (!userVm.state.value.isLoaded) {
            visitorVm.getVisitors()
            userVm.getUser { parkingVm.getParking() }
        } else if (statsStore.shouldRequestReview()) {
            statsStore.markRequested()
            context.findActivity()?.let { appReview.request(it) }
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            parkingVm.getParking()
            userVm.getBalance()
        }
    }

    UserContent(
        balance = user.balance,
        parking = parking,
        visitors = visitors,
        actions = UserActions(
            onLogout = onLogout,
            onInfo = onInfo,
            onHistory = onHistory,
            onPayment = onPayment,
            onAccounts = onAccounts,
            onSettings = onSettings,
            onAddVisitor = onAddVisitor,
            onAddParking = onAddParking,
            onDeleteVisitor = { visitorVm.deleteVisitor(it) },
            onTooManyVisitors = { messageBus.show(tooManyMsg, MessageType.WARN) },
            onBalanceTap = { userVm.getBalance() },
            onStop = { p -> parkingVm.stopParking(p) { userVm.getBalance() } },
        ),
    )
}

@Composable
internal fun UserContent(
    balance: String?,
    parking: ParkingResponse?,
    visitors: List<Visitor>?,
    actions: UserActions,
) {
    Column(Modifier.fillMaxSize()) {
        HeaderView(
            loggedIn = true,
            balance = balance,
            onBalanceTap = actions.onBalanceTap,
            onInfo = actions.onInfo,
            onHistory = actions.onHistory,
            onPayment = actions.onPayment,
            onAccounts = actions.onAccounts,
            onSettings = actions.onSettings,
            onLogout = actions.onLogout,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(Dimens.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        ) {
            item { SectionHeader(stringResource(R.string.parking_header)) }
            val active = parking?.active.orEmpty()
            val scheduled = parking?.scheduled.orEmpty()
            if (active.isEmpty() && scheduled.isEmpty()) {
                item { Text(stringResource(R.string.parking_no_sessions)) }
            } else {
                if (active.isNotEmpty()) {
                    item { Text(stringResource(R.string.parking_active)) }
                    items(active, key = { it.id }) { p ->
                        ParkingRow(p, onStop = { actions.onStop(p) })
                    }
                }
                if (scheduled.isNotEmpty()) {
                    item { Text(stringResource(R.string.parking_scheduled)) }
                    items(scheduled, key = { it.id }) { p ->
                        ParkingRow(p, onStop = { actions.onStop(p) })
                    }
                }
            }

            item { HorizontalDivider(Modifier.padding(vertical = Dimens.paddingSmall)) }
            item { SectionHeader(stringResource(R.string.visitor_header)) }
            if (visitors != null && visitors.isEmpty()) {
                item { Text(stringResource(R.string.visitor_no_visitors)) }
            } else {
                items(visitors.orEmpty(), key = { it.id }) { v ->
                    VisitorRow(
                        visitor = v,
                        onClick = { actions.onAddParking(v.id) },
                        onDelete = { actions.onDeleteVisitor(v) },
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        if ((visitors?.size ?: 0) >= MAX_VISITORS) actions.onTooManyVisitors() else actions.onAddVisitor()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.success,
                        contentColor = AppTheme.colors.enabled,
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = Dimens.paddingSmall),
                ) { Text(stringResource(R.string.visitor_add)) }
            }
        }
    }
}

/**
 * Full-swipe (end-to-start) row that triggers [onAction] with a red [actionLabel] background.
 * Used for visitor delete and parking stop (iOS `swipeActions`).
 */
@Composable
private fun SwipeToActionRow(
    actionLabel: String,
    onAction: () -> Unit,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) onAction()
    }
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.danger)
                    .padding(horizontal = Dimens.paddingNormal),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(actionLabel, color = AppTheme.colors.enabled, fontWeight = FontWeight.SemiBold)
            }
        },
    ) {
        Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) { content() }
    }
}

@Composable
private fun VisitorRow(visitor: Visitor, onClick: () -> Unit, onDelete: () -> Unit) {
    SwipeToActionRow(actionLabel = stringResource(R.string.common_delete), onAction = onDelete) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = Dimens.paddingMini),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        ) {
            LicensePlate(visitor.license)
            Text(visitor.name ?: "", style = AppType.name)
        }
    }
}

@Composable
private fun ParkingRow(parking: Parking, onStop: () -> Unit) {
    SwipeToActionRow(actionLabel = stringResource(R.string.common_stop), onAction = onStop) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.paddingMini),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        ) {
            LicensePlate(parking.license)
            Text(VisitorNameCache.map[parking.license] ?: "", style = AppType.name)
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun UserContentPreview() = ParkeerAssistentTheme {
    val parking = ParkingResponse(
        active = listOf(Parking(1, "12ABC3", "Jan", "2026-05-29T14:43:00+02:00", "2026-05-29T14:58:00+02:00", 0.53)),
        scheduled = emptyList(),
    )
    val visitors = listOf(
        Visitor(1, "22BBB2", "22-BBB-2", "Erik"),
        Visitor(2, "111AA1", "111-AA-1", "Suzanne"),
    )
    UserContent(
        balance = "20.27",
        parking = parking,
        visitors = visitors,
        actions = UserActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}),
    )
}
