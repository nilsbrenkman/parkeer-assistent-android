package nl.parkeerassistent.amsterdam.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Named text styles ported from iOS `Font.ui`. */
object AppType {
    val license = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
    val name = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
    val dataBoxTitle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Normal)
    val dataBoxContent = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Normal)
    val calendarDow = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold)
    val calendarDay = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
    val sectionHeader = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
}
