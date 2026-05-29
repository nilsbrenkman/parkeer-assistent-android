package nl.parkeerassistent.amsterdam.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

/**
 * Inline screen title (iOS `pageTitle`, minus the back button). Navigation back is handled by the
 * system back button/gesture via the NavHost, so no explicit affordance is needed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleBar(title: String) {
    TopAppBar(title = { Text(title) })
}
