package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.parkeerassistent.amsterdam.ui.components.TitleBar

/** Stand-in for screens not yet built (filled in during the Phase 6 fan-out). */
@Composable
fun PlaceholderScreen(title: String) {
    Column(Modifier.fillMaxSize()) {
        TitleBar(title = title)
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("$title — nog niet geïmplementeerd")
        }
    }
}
