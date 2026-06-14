package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsContentTest {

    @get:Rule val rule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private fun s(id: Int) = ctx.getString(id)

    @Test fun rendersSectionsAndLabels() {
        rule.setContent {
            ParkeerAssistentTheme { SettingsContent(true, true, true, 1f, true, {}, {}, {}, {}, {}) }
        }
        // SectionHeader appends a trailing colon, so match as a substring.
        rule.onNodeWithText(s(R.string.settings_notifications), substring = true).assertExists()
        rule.onNodeWithText(s(R.string.settings_on_start)).assertExists()
        rule.onNodeWithText(s(R.string.account_auto_login)).assertExists()
    }

    @Test fun switchesReflectCheckedState() {
        rule.setContent {
            ParkeerAssistentTheme { SettingsContent(false, true, true, 1f, false, {}, {}, {}, {}, {}) }
        }
        val switches = rule.onAllNodes(isToggleable())
        // Order matches the column: onStart, onStop, reminders, autoLogin
        switches[0].assertIsOff()
        switches[1].assertIsOn()
        switches[3].assertIsOff()
    }

    @Test fun togglingOnStartInvokesCallback() {
        var changed: Boolean? = null
        rule.setContent {
            ParkeerAssistentTheme {
                SettingsContent(false, true, true, 1f, true, { changed = it }, {}, {}, {}, {})
            }
        }
        rule.onAllNodes(isToggleable())[0].performClick()
        assertEquals(true, changed)
    }
}
