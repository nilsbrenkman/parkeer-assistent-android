package nl.parkeerassistent.amsterdam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.AppType
import nl.parkeerassistent.amsterdam.ui.theme.Dimens

/** Horizontally centers its content, filling the available width (iOS `Centered`). */
@Composable
fun Centered(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** Shows a spinner while [wait], otherwise the content, centered (iOS `ButtonWait`). */
@Composable
fun ButtonWait(wait: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Centered(modifier) {
        if (wait) CircularProgressIndicator() else content()
    }
}

/** "Header:" in the section-header style, de-emphasized (iOS `SectionHeader`). */
@Composable
fun SectionHeader(header: String, modifier: Modifier = Modifier) {
    Text(
        text = "$header:",
        style = AppType.sectionHeader,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

/** Title above a bordered, rounded value box (iOS `DataBox`). */
@Composable
fun DataBox(title: String, content: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Dimens.spacingXSmall)) {
        Text("$title:", style = AppType.dataBoxTitle)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(Dimens.radiusSmall))
                .background(AppTheme.colors.bw70)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(Dimens.radiusSmall)),
            contentAlignment = Alignment.Center,
        ) {
            Text(content, style = AppType.dataBoxContent)
        }
    }
}

/** Label on the left, secondary value right-aligned (iOS `Property`). */
@Composable
fun Property(label: String, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.paddingMini),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}
