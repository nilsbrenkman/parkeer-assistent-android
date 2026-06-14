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
import nl.parkeerassistent.amsterdam.data.model.Credentials
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountContentTest {

    @get:Rule val rule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private fun s(id: Int) = ctx.getString(id)

    @Test fun showsEmptyStateWhenNoAccounts() {
        rule.setContent {
            ParkeerAssistentTheme { AccountsContent(emptyList(), {}, {}, {}) }
        }
        rule.onNodeWithText(s(R.string.account_empty)).assertExists()
    }

    @Test fun rendersAccountsUsingAliasThenUsername() {
        rule.setContent {
            ParkeerAssistentTheme {
                AccountsContent(
                    listOf(Credentials("Thuis", "user1", "x"), Credentials(null, "user2", "y")),
                    {}, {}, {},
                )
            }
        }
        rule.onNodeWithText("Thuis").assertExists()
        rule.onNodeWithText("user2").assertExists()
    }

    @Test fun openInvokesCallbackWithUsername() {
        var opened = ""
        rule.setContent {
            ParkeerAssistentTheme {
                AccountsContent(listOf(Credentials("Thuis", "user1", "x")), { opened = it }, {}, {})
            }
        }
        rule.onNodeWithText("Thuis").performClick()
        assertEquals("user1", opened)
    }

    @Test fun addInvokesCallback() {
        var added = false
        rule.setContent {
            ParkeerAssistentTheme { AccountsContent(emptyList(), {}, { added = true }, {}) }
        }
        rule.onNode(hasText(s(R.string.common_add)) and hasClickAction()).performClick()
        assertTrue(added)
    }

    @Test fun detailSaveDisabledWhenCredentialsEmpty() {
        rule.setContent {
            ParkeerAssistentTheme { AccountDetailContent(true, "", "", "", {}, {}, {}, {}) }
        }
        rule.onNode(hasText(s(R.string.common_save)) and hasClickAction()).assertIsNotEnabled()
    }

    @Test fun detailSaveEnabledAndInvokesCallback() {
        var saved = false
        rule.setContent {
            ParkeerAssistentTheme {
                AccountDetailContent(false, "Thuis", "user1", "secret", {}, {}, {}, { saved = true })
            }
        }
        val button = rule.onNode(hasText(s(R.string.common_save)) and hasClickAction())
        button.assertIsEnabled()
        button.performClick()
        assertTrue(saved)
    }
}
