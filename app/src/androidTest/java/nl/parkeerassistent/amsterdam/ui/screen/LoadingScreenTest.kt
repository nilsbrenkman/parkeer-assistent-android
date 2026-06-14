package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoadingScreenTest {

    @get:Rule val rule = createComposeRule()

    @Test fun showsIndeterminateProgressIndicator() {
        rule.setContent {
            ParkeerAssistentTheme { LoadingScreen() }
        }
        rule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo.Indeterminate,
            ),
        ).assertExists()
    }
}
