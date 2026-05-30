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
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginContentTest {

    @get:Rule val rule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private fun s(id: Int) = ctx.getString(id)

    @Test fun loginButtonDisabledWhenCredentialsEmpty() {
        rule.setContent {
            ParkeerAssistentTheme {
                LoginContent("", "", false, false, emptyList(), true, false, {}, {}, {}, {}, {}, {})
            }
        }
        rule.onNode(hasText(s(R.string.login_login)) and hasClickAction()).assertIsNotEnabled()
    }

    @Test fun loginButtonEnabledAndInvokesCallback() {
        var clicked = false
        rule.setContent {
            ParkeerAssistentTheme {
                LoginContent("test", "1234", false, false, emptyList(), true, false, {}, {}, {}, {}, {}, { clicked = true })
            }
        }
        val button = rule.onNode(hasText(s(R.string.login_login)) and hasClickAction())
        button.assertIsEnabled()
        button.performClick()
        assertTrue(clicked)
    }

    @Test fun rememberToggleShownWhenBiometricAvailable() {
        rule.setContent {
            ParkeerAssistentTheme {
                LoginContent("", "", false, false, emptyList(), true, false, {}, {}, {}, {}, {}, {})
            }
        }
        rule.onNodeWithText(s(R.string.login_remember_short)).assertExists()
    }
}
