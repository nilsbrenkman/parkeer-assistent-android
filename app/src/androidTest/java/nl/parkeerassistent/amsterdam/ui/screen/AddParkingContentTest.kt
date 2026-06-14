package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.data.model.Visitor
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddParkingContentTest {

    @get:Rule val rule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private fun s(id: Int) = ctx.getString(id)

    private val visitor = Visitor(1, "22BBB2", "22-BBB-2", "Erik")

    private fun content(addEnabled: Boolean, onAdd: () -> Unit = {}, onPickMeter: () -> Unit = {}) {
        rule.setContent {
            ParkeerAssistentTheme {
                AddParkingContent(
                    visitor = visitor,
                    dateText = "29 mei",
                    startTimeText = "17:00",
                    endTimeText = "17:14",
                    minutesText = "13",
                    costText = "0.51",
                    meterId = 55105,
                    dateActive = false,
                    startTimeActive = false,
                    addEnabled = addEnabled,
                    onDateClick = {},
                    onStartTimeClick = {},
                    onMinutesClick = {},
                    onPickMeter = onPickMeter,
                    onWheel = {},
                    onAdd = onAdd,
                )
            }
        }
    }

    @Test fun rendersVisitorAndParkingData() {
        content(addEnabled = true)
        rule.onNodeWithText("Erik").assertExists()
        rule.onNodeWithText("17:00").assertExists()
        rule.onNodeWithText("17:14").assertExists()
        rule.onNodeWithText("13").assertExists()
        rule.onNodeWithText("€ 0.51").assertExists()
        rule.onNodeWithText("55105").assertExists()
    }

    @Test fun addButtonDisabledWhenNotEnabled() {
        content(addEnabled = false)
        rule.onNode(hasText(s(R.string.common_add)) and hasClickAction()).assertIsNotEnabled()
    }

    @Test fun addButtonEnabledAndInvokesCallback() {
        var added = false
        content(addEnabled = true, onAdd = { added = true })
        val button = rule.onNode(hasText(s(R.string.common_add)) and hasClickAction())
        button.assertIsEnabled()
        button.performClick()
        assertTrue(added)
    }

    @Test fun tappingSignBoxInvokesPickMeter() {
        var picked = false
        content(addEnabled = true, onPickMeter = { picked = true })
        rule.onNodeWithText("55105").performClick()
        assertTrue(picked)
    }
}
