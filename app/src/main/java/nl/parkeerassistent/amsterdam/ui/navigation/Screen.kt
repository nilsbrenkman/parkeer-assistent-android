package nl.parkeerassistent.amsterdam.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes (port of iOS `Router`'s `Screen` enum), used with
 * Navigation-Compose. The NavHost wiring lives in Phase 6 alongside the screens.
 *
 * iOS `account(Credentials)` is omitted until Phase 7 (Keychain credentials); when added it will
 * be keyed by username. `addParking` carries the visitor id (the screen resolves the Visitor).
 */
sealed interface Screen {
    @Serializable data object Login : Screen
    @Serializable data object User : Screen
    @Serializable data object Info : Screen
    @Serializable data object History : Screen
    @Serializable data class HistoryDetail(val parkingId: Long) : Screen
    @Serializable data object Payment : Screen
    @Serializable data object Accounts : Screen
    @Serializable data class AccountDetail(val username: String) : Screen
    @Serializable data object Settings : Screen
    @Serializable data object AddVisitor : Screen
    @Serializable data class AddParking(val visitorId: Long) : Screen
    @Serializable data object ParkingMeter : Screen
}
