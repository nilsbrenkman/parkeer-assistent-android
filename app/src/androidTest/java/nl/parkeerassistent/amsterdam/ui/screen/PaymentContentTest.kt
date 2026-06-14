package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentContentTest {

    @get:Rule val rule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private fun s(id: Int) = ctx.getString(id)

    @Test fun payButtonDisabledUntilAmountAndMethodSelected() {
        rule.setContent {
            ParkeerAssistentTheme { PaymentContent(0, "", false, {}, {}, {}) }
        }
        rule.onNode(hasText(s(R.string.payment_start)) and hasClickAction()).assertIsNotEnabled()
    }

    @Test fun payButtonEnabledAndInvokesCallback() {
        var paid = false
        rule.setContent {
            ParkeerAssistentTheme { PaymentContent(500, "IDEAL", false, {}, {}, { paid = true }) }
        }
        val button = rule.onNode(hasText(s(R.string.payment_start)) and hasClickAction())
        button.assertIsEnabled()
        button.performClick()
        assertTrue(paid)
    }

    @Test fun paymentInProgressReplacesLabelWithSpinner() {
        // While a payment is in flight the SuccessButton is disabled and swaps its label for a
        // progress spinner, so the "Start" text is no longer present.
        rule.setContent {
            ParkeerAssistentTheme { PaymentContent(500, "IDEAL", true, {}, {}, {}) }
        }
        rule.onNodeWithText(s(R.string.payment_start)).assertDoesNotExist()
    }

    @Test fun amountButtonInvokesCallbackWithCents() {
        var picked = -1
        rule.setContent {
            ParkeerAssistentTheme { PaymentContent(0, "", false, { picked = it }, {}, {}) }
        }
        rule.onNodeWithText("€ 5.00").performClick()
        assertEquals(500, picked)
    }

    @Test fun methodButtonInvokesCallbackWithKey() {
        var method = ""
        rule.setContent {
            ParkeerAssistentTheme { PaymentContent(0, "", false, {}, { method = it }, {}) }
        }
        rule.onNodeWithText("iDEAL | Wero").performClick()
        assertEquals("IDEAL", method)
    }
}
