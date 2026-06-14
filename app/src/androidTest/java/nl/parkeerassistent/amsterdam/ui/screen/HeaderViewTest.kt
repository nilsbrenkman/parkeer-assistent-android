package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HeaderViewTest {

    @get:Rule val rule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private fun s(id: Int) = ctx.getString(id)

    @Test fun loggedOutHidesBalanceAndMenu() {
        rule.setContent {
            ParkeerAssistentTheme { HeaderView(loggedIn = false, balance = "12,34") }
        }
        rule.onNodeWithText("12,34", substring = true).assertDoesNotExist()
        rule.onNodeWithText("≡").assertDoesNotExist()
    }

    @Test fun loggedInShowsBalance() {
        rule.setContent {
            ParkeerAssistentTheme { HeaderView(loggedIn = true, balance = "12,34") }
        }
        rule.onNodeWithText("12,34", substring = true).assertIsDisplayed()
    }

    @Test fun tappingLogoInvokesOnInfo() {
        var info = false
        rule.setContent {
            ParkeerAssistentTheme { HeaderView(loggedIn = false, onInfo = { info = true }) }
        }
        rule.onNodeWithContentDescription(s(R.string.app_name)).performClick()
        assertTrue(info)
    }

    @Test fun tappingBalanceRowInvokesOnBalanceTap() {
        var tapped = false
        rule.setContent {
            ParkeerAssistentTheme {
                HeaderView(loggedIn = true, balance = "12,34", onBalanceTap = { tapped = true })
            }
        }
        rule.onNodeWithText("12,34", substring = true).performClick()
        assertTrue(tapped)
    }

    @Test fun menuItemsInvokeTheirCallbacks() {
        var history = false
        var payment = false
        var accounts = false
        var settings = false
        var logout = false
        rule.setContent {
            ParkeerAssistentTheme {
                HeaderView(
                    loggedIn = true,
                    balance = "12,34",
                    onHistory = { history = true },
                    onPayment = { payment = true },
                    onAccounts = { accounts = true },
                    onSettings = { settings = true },
                    onLogout = { logout = true },
                )
            }
        }

        // Each menu item closes the dropdown, so re-open before the next tap.
        tapMenuItem(s(R.string.parking_history)); assertTrue(history)
        tapMenuItem(s(R.string.user_add_balance)); assertTrue(payment)
        tapMenuItem(s(R.string.account_header)); assertTrue(accounts)
        tapMenuItem(s(R.string.settings_header)); assertTrue(settings)
        tapMenuItem(s(R.string.login_logout)); assertTrue(logout)
    }

    private fun tapMenuItem(label: String) {
        rule.onNodeWithText("≡").performClick()
        rule.onNodeWithText(label).performClick()
    }
}
