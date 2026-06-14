package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.data.model.Parking
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParkingDetailContentTest {

    @get:Rule val rule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private fun s(id: Int) = ctx.getString(id)

    private val parking = Parking(id = 1, license = "12ABC3", name = "Jan", startTime = "2026-05-29T14:00:00+02:00", endTime = "2026-05-29T15:00:00+02:00", cost = 1.20)

    @Test fun showsPlaceholderWhenParkingMissing() {
        rule.setContent {
            ParkeerAssistentTheme { ParkingDetailContent(null, "", {}) }
        }
        rule.onNodeWithText(s(R.string.parking_no_history)).assertExists()
    }

    @Test fun rendersDetailsAndStopButton() {
        rule.setContent {
            ParkeerAssistentTheme { ParkingDetailContent(parking, "Jan Jansen", {}) }
        }
        rule.onNodeWithText("12-ABC-3").assertExists()
        rule.onNodeWithText("Jan Jansen").assertExists()
        rule.onNodeWithText("€ 1.20").assertExists()
        rule.onNode(hasText(s(R.string.parking_stop)) and hasClickAction()).assertExists()
    }

    @Test fun stopButtonInvokesCallback() {
        var stopped = false
        rule.setContent {
            ParkeerAssistentTheme { ParkingDetailContent(parking, "Jan Jansen") { stopped = true } }
        }
        rule.onNode(hasText(s(R.string.parking_stop)) and hasClickAction()).performClick()
        assertTrue(stopped)
    }
}
