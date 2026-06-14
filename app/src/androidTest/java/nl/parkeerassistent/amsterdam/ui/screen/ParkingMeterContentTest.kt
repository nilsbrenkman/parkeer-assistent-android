package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import nl.parkeerassistent.amsterdam.data.model.ParkingMeter
import nl.parkeerassistent.amsterdam.data.model.ParkingMeterType
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParkingMeterContentTest {

    @get:Rule val rule = createComposeRule()

    private val meters = listOf(
        ParkingMeter(55105, 1, "Nieuwmarkt", ParkingMeterType.METER, 52.3725, 4.9005, 80.0),
        ParkingMeter(55106, 1, "Waterlooplein", ParkingMeterType.SIGN, 52.3680, 4.9020, 240.0),
    )

    @Test fun rendersMeterNamesIdsAndDistances() {
        rule.setContent {
            ParkeerAssistentTheme { ParkingMeterContent(meters) {} }
        }
        rule.onNodeWithText("Nieuwmarkt").assertExists()
        rule.onNodeWithText("Waterlooplein").assertExists()
        rule.onNodeWithText("55105").assertExists()
        rule.onNodeWithText("80 m").assertExists()
    }

    @Test fun selectingMeterInvokesCallback() {
        var selected: ParkingMeter? = null
        rule.setContent {
            ParkeerAssistentTheme { ParkingMeterContent(meters) { selected = it } }
        }
        rule.onNodeWithText("Waterlooplein").performClick()
        assertEquals(55106, selected?.id)
    }
}
