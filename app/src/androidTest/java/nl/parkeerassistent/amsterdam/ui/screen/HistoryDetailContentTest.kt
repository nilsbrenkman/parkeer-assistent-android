package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.data.model.Parking
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryDetailContentTest {

    @get:Rule val rule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private fun s(id: Int) = ctx.getString(id)

    private val parking = Parking(id = 1, license = "12ABC3", name = "Jan", startTime = "2026-05-29T14:00:00+02:00", endTime = "2026-05-29T15:00:00+02:00", cost = 1.20)

    @Test fun showsPlaceholderWhenParkingMissing() {
        rule.setContent {
            ParkeerAssistentTheme { HistoryDetailContent(null, "") }
        }
        rule.onNodeWithText(s(R.string.parking_no_history)).assertExists()
    }

    @Test fun rendersLicenseNameAndCost() {
        rule.setContent {
            ParkeerAssistentTheme { HistoryDetailContent(parking, "Jan Jansen") }
        }
        rule.onNodeWithText("12-ABC-3").assertExists()
        rule.onNodeWithText("Jan Jansen").assertExists()
        rule.onNodeWithText("€ 1.20").assertExists()
    }
}
