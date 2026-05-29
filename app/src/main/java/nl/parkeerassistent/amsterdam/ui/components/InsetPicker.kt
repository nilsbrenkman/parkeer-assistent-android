package nl.parkeerassistent.amsterdam.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.Dimens

/** List of options with a checkmark on the selected one (iOS `InsetPicker`). */
@Composable
fun InsetPicker(
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        labels.forEachIndexed { index, label ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(index) }
                    .padding(horizontal = Dimens.paddingNormal, vertical = Dimens.paddingSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = label, modifier = Modifier.weight(1f))
                if (index == selected) {
                    Text("✓", color = AppTheme.colors.header)
                }
            }
        }
    }
}
