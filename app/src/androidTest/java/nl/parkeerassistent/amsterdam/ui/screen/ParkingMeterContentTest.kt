package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import nl.parkeerassistent.amsterdam.data.model.ParkingMeter
import nl.parkeerassistent.amsterdam.data.model.ParkingMeterType
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The meter picker is a MapLibre GL map (markers/selection live on the GL surface, not in the
 * Compose tree), so this only asserts the surrounding chrome renders. Selection logic is covered
 * by `ParkingMeterViewModel` unit tests; the map itself needs the emulator + a live tile host
 * (see docs/TO_DO.md) to verify visually.
 */
@RunWith(AndroidJUnit4::class)
class ParkingMeterContentTest {

    @get:Rule val rule = createComposeRule()

    private val meters = listOf(
        ParkingMeter(55105, 1, "Nieuwmarkt", ParkingMeterType.METER, 52.3725, 4.9005, 80.0),
        ParkingMeter(55106, 1, "Waterlooplein", ParkingMeterType.SIGN, 52.3680, 4.9020, 240.0),
    )

    @Test fun rendersTitleBar() {
        rule.setContent {
            ParkeerAssistentTheme { ParkingMeterContent(meters = meters) }
        }
        rule.onNodeWithText("Sign").assertExists()
    }
}
