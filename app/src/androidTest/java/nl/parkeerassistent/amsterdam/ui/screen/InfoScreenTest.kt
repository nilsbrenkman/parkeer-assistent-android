package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InfoScreenTest {

    @get:Rule val rule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private fun s(id: Int) = ctx.getString(id)

    @Test fun rendersHeaderAndExternalLinks() {
        rule.setContent {
            ParkeerAssistentTheme { InfoScreen() }
        }
        rule.onNodeWithText(s(R.string.info_header)).assertExists()
        rule.onNodeWithText(s(R.string.info_website)).assertExists()
        rule.onNodeWithText(s(R.string.info_source_code)).assertExists()
        rule.onNodeWithText(s(R.string.info_feedback)).assertExists()
    }

    @Test fun showsVersionLine() {
        rule.setContent {
            ParkeerAssistentTheme { InfoScreen() }
        }
        rule.onNodeWithText("${s(R.string.info_version)}:", substring = true).assertExists()
    }
}
