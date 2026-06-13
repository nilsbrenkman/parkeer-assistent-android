package nl.parkeerassistent.amsterdam.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import nl.parkeerassistent.amsterdam.ui.account.AccountViewModel
import nl.parkeerassistent.amsterdam.ui.parking.ParkingViewModel
import nl.parkeerassistent.amsterdam.ui.screen.AccountDetailScreen
import nl.parkeerassistent.amsterdam.ui.screen.AccountsScreen
import nl.parkeerassistent.amsterdam.ui.screen.AddParkingScreen
import nl.parkeerassistent.amsterdam.ui.screen.AddVisitorScreen
import nl.parkeerassistent.amsterdam.ui.screen.HistoryDetailScreen
import nl.parkeerassistent.amsterdam.ui.screen.HistoryListScreen
import nl.parkeerassistent.amsterdam.ui.screen.InfoScreen
import nl.parkeerassistent.amsterdam.ui.screen.ParkingDetailScreen
import nl.parkeerassistent.amsterdam.ui.screen.ParkingMeterScreen
import nl.parkeerassistent.amsterdam.ui.screen.PaymentScreen
import nl.parkeerassistent.amsterdam.ui.screen.SettingsScreen
import nl.parkeerassistent.amsterdam.ui.screen.UserScreen
import nl.parkeerassistent.amsterdam.ui.theme.Dimens
import nl.parkeerassistent.amsterdam.ui.user.UserViewModel
import nl.parkeerassistent.amsterdam.ui.visitor.VisitorViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Navigation graph for the logged-in area, rooted at [Screen.User] (iOS `Router` + `ContentView`
 * destinations). The user/visitor/parking ViewModels are obtained here — above the per-destination
 * `ViewModelStoreOwner`s — so every screen shares one instance instead of getting its own.
 */
@Composable
fun AppNavHost(onLogout: () -> Unit) {
    val nav = rememberNavController()

    val userVm: UserViewModel = koinViewModel()
    val visitorVm: VisitorViewModel = koinViewModel()
    val parkingVm: ParkingViewModel = koinViewModel()
    val accountVm: AccountViewModel = koinViewModel()

    NavHost(navController = nav, startDestination = Screen.User) {
        composable<Screen.User> {
            UserScreen(
                onLogout = onLogout,
                onInfo = { nav.navigate(Screen.Info) },
                onHistory = { nav.navigate(Screen.History) },
                onPayment = { nav.navigate(Screen.Payment) },
                onAccounts = { nav.navigate(Screen.Accounts) },
                onSettings = { nav.navigate(Screen.Settings) },
                onAddVisitor = { nav.navigate(Screen.AddVisitor) },
                onAddParking = { visitorId -> nav.navigate(Screen.AddParking(visitorId)) },
                onOpenParking = { id -> nav.navigate(Screen.ParkingDetail(id)) },
                userVm = userVm,
                visitorVm = visitorVm,
                parkingVm = parkingVm,
            )
        }
        composable<Screen.AddVisitor> {
            AddVisitorScreen(visitorVm = visitorVm, onBack = nav::popBackStack)
        }
        composable<Screen.AddParking> { backStackEntry ->
            val visitorId = backStackEntry.toRoute<Screen.AddParking>().visitorId
            val visitors by visitorVm.visitors.collectAsStateWithLifecycle()
            val visitor = visitors?.firstOrNull { it.id == visitorId }
            if (visitor == null) {
                Spacer(Modifier.height(Dimens.spacingLarge))
            } else {
                AddParkingScreen(
                    visitor = visitor,
                    userVm = userVm,
                    parkingVm = parkingVm,
                    onBack = nav::popBackStack,
                    onPickMeter = { nav.navigate(Screen.ParkingMeter) },
                )
            }
        }
        composable<Screen.ParkingMeter> {
            ParkingMeterScreen(userVm = userVm, onBack = nav::popBackStack)
        }
        composable<Screen.Info> { InfoScreen() }
        composable<Screen.History> {
            HistoryListScreen(
                parkingVm = parkingVm,
                onOpen = { id -> nav.navigate(Screen.HistoryDetail(id)) },
            )
        }
        composable<Screen.HistoryDetail> { backStackEntry ->
            HistoryDetailScreen(
                parkingId = backStackEntry.toRoute<Screen.HistoryDetail>().parkingId,
                parkingVm = parkingVm,
                visitorVm = visitorVm,
            )
        }
        composable<Screen.ParkingDetail> { backStackEntry ->
            ParkingDetailScreen(
                parkingId = backStackEntry.toRoute<Screen.ParkingDetail>().parkingId,
                parkingVm = parkingVm,
                visitorVm = visitorVm,
                onBack = nav::popBackStack,
            )
        }
        composable<Screen.Payment> { PaymentScreen() }
        composable<Screen.Accounts> {
            AccountsScreen(
                accountVm = accountVm,
                onOpen = { username -> nav.navigate(Screen.AccountDetail(username)) },
                onAdd = { nav.navigate(Screen.AccountDetail("")) },
                onAuthFailed = nav::popBackStack,
            )
        }
        composable<Screen.AccountDetail> { backStackEntry ->
            AccountDetailScreen(
                username = backStackEntry.toRoute<Screen.AccountDetail>().username,
                accountVm = accountVm,
                onBack = nav::popBackStack,
            )
        }
        composable<Screen.Settings> { SettingsScreen() }
    }
}
