package nl.parkeerassistent.amsterdam.ui.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.parkeerassistent.amsterdam.data.model.Visitor
import nl.parkeerassistent.amsterdam.ui.components.ButtonWait
import nl.parkeerassistent.amsterdam.ui.components.CalendarDate
import nl.parkeerassistent.amsterdam.ui.components.Centered
import nl.parkeerassistent.amsterdam.ui.components.DataBox
import nl.parkeerassistent.amsterdam.ui.components.InsetPicker
import nl.parkeerassistent.amsterdam.ui.components.LicensePlate
import nl.parkeerassistent.amsterdam.ui.components.ModalOverlay
import nl.parkeerassistent.amsterdam.ui.components.Property
import nl.parkeerassistent.amsterdam.ui.components.SectionHeader
import nl.parkeerassistent.amsterdam.ui.components.TitleBar
import nl.parkeerassistent.amsterdam.ui.components.VisitorView
import nl.parkeerassistent.amsterdam.ui.components.WheelSelector
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import java.time.LocalDate

private val sampleVisitor = Visitor(id = 1, license = "12ABC3", formattedLicense = "12-ABC-3", name = "Jan Jansen")

@Preview(showBackground = true)
@Composable
private fun LicensePlatePreview() = ParkeerAssistentTheme { LicensePlate("12-ABC-3", Modifier.padding(8.dp)) }

@Preview(showBackground = true)
@Composable
private fun VisitorViewPreview() = ParkeerAssistentTheme { VisitorView(sampleVisitor, Modifier.padding(8.dp)) }

@Preview(showBackground = true)
@Composable
private fun CalendarDatePreview() = ParkeerAssistentTheme { CalendarDate(LocalDate.of(2026, 5, 29)) }

@Preview(showBackground = true)
@Composable
private fun SectionHeaderPreview() = ParkeerAssistentTheme { SectionHeader("Parkeren", Modifier.padding(8.dp)) }

@Preview(showBackground = true)
@Composable
private fun DataBoxPreview() = ParkeerAssistentTheme { DataBox("Kosten", "€ 12.34", Modifier.padding(8.dp)) }

@Preview(showBackground = true)
@Composable
private fun PropertyPreview() = ParkeerAssistentTheme { Property("Naam", "Jan Jansen", Modifier.padding(8.dp)) }

@Preview(showBackground = true)
@Composable
private fun CenteredPreview() = ParkeerAssistentTheme { Centered(Modifier.padding(8.dp)) { Text("Gecentreerd") } }

@Preview(showBackground = true)
@Composable
private fun ButtonWaitPreview() = ParkeerAssistentTheme {
    Column(Modifier.padding(8.dp)) {
        ButtonWait(wait = false) { Text("Klaar") }
        ButtonWait(wait = true) { Text("Bezig") }
    }
}

@Preview(showBackground = true)
@Composable
private fun TitleBarPreview() = ParkeerAssistentTheme { TitleBar("Instellingen") }

@Preview(showBackground = true)
@Composable
private fun InsetPickerPreview() = ParkeerAssistentTheme {
    InsetPicker(labels = listOf("15 m", "30 m", "1 u"), selected = 1, onSelect = {})
}

@Preview(showBackground = true, heightDp = 220)
@Composable
private fun WheelSelectorPreview() = ParkeerAssistentTheme { WheelSelector(onChange = {}) }

@Preview(showBackground = true, heightDp = 220)
@Composable
private fun ModalOverlayPreview() = ParkeerAssistentTheme {
    ModalOverlay(visible = true, onClose = {}) {
        DataBox("Titel", "Inhoud", Modifier.padding(24.dp))
    }
}
