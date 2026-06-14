package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.data.model.Parking
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryListContentTest {

    @get:Rule val rule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private fun s(id: Int) = ctx.getString(id)

    private val sample = listOf(
        Parking(id = 1, license = "12ABC3", name = "Jan", startTime = "2026-05-29T14:00:00+02:00", endTime = "2026-05-29T15:00:00+02:00", cost = 1.20),
        Parking(id = 2, license = "55ABC6", name = "Erik", startTime = "2026-04-12T10:00:00+02:00", endTime = "2026-04-12T11:30:00+02:00", cost = 2.10),
    )

    @Test fun showsEmptyMessageWhenHistoryEmpty() {
        rule.setContent {
            ParkeerAssistentTheme { HistoryListContent(emptyList()) {} }
        }
        rule.onNodeWithText(s(R.string.parking_no_history)).assertExists()
    }

    @Test fun rendersFormattedLicensePlates() {
        rule.setContent {
            ParkeerAssistentTheme { HistoryListContent(sample) {} }
        }
        rule.onNodeWithText("12-ABC-3").assertExists()
    }

    @Test fun tappingRowInvokesOpenWithId() {
        var opened = -1L
        rule.setContent {
            ParkeerAssistentTheme { HistoryListContent(sample) { opened = it } }
        }
        rule.onNodeWithText("12-ABC-3").performClick()
        assertEquals(1L, opened)
    }
}
