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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddVisitorContentTest {

    @get:Rule val rule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private fun s(id: Int) = ctx.getString(id)

    @Test fun addButtonDisabledWhenFieldsEmpty() {
        rule.setContent { ParkeerAssistentTheme { AddVisitorContent("", "", {}, {}, {}) } }
        rule.onNode(hasText(s(R.string.common_add)) and hasClickAction()).assertIsNotEnabled()
    }

    @Test fun addButtonEnabledAndInvokesCallback() {
        var added = false
        rule.setContent {
            ParkeerAssistentTheme { AddVisitorContent("12-ABC-3", "Jan", {}, {}, { added = true }) }
        }
        val button = rule.onNode(hasText(s(R.string.common_add)) and hasClickAction())
        button.assertIsEnabled()
        button.performClick()
        assertTrue(added)
    }

    @Test fun showsFieldLabels() {
        rule.setContent { ParkeerAssistentTheme { AddVisitorContent("", "", {}, {}, {}) } }
        rule.onNodeWithText(s(R.string.visitor_license)).assertExists()
        rule.onNodeWithText(s(R.string.visitor_name)).assertExists()
    }
}
