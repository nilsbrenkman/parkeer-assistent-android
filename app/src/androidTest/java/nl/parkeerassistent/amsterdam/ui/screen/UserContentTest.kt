package nl.parkeerassistent.amsterdam.ui.screen

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
class UserContentTest {

    @get:Rule val rule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private fun s(id: Int) = ctx.getString(id)

    private fun actions(onAddVisitor: () -> Unit = {}) = UserActions(
        onLogout = {}, onInfo = {}, onHistory = {}, onPayment = {}, onAccounts = {}, onSettings = {},
        onAddVisitor = onAddVisitor, onAddParking = {}, onDeleteVisitor = {}, onTooManyVisitors = {},
        onBalanceTap = {}, onStop = {}, onOpenParking = {},
    )

    @Test fun rendersVisitorAndEmptyParking() {
        rule.setContent {
            ParkeerAssistentTheme {
                UserContent("20.27", null, listOf(Visitor(1, "22BBB2", "22-BBB-2", "Erik")), actions())
            }
        }
        rule.onNodeWithText("Erik").assertExists()
        rule.onNodeWithText("22-BBB-2").assertExists()
        rule.onNodeWithText(s(R.string.parking_no_sessions)).assertExists()
    }

    @Test fun addVisitorButtonInvokesCallback() {
        var addVisitor = false
        rule.setContent {
            ParkeerAssistentTheme {
                UserContent("20.27", null, emptyList(), actions(onAddVisitor = { addVisitor = true }))
            }
        }
        rule.onNode(hasText(s(R.string.visitor_add)) and hasClickAction()).performClick()
        assertTrue(addVisitor)
    }
}
