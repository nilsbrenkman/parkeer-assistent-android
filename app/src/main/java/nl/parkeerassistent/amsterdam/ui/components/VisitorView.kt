package nl.parkeerassistent.amsterdam.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import nl.parkeerassistent.amsterdam.data.model.Visitor
import nl.parkeerassistent.amsterdam.ui.theme.AppType
import nl.parkeerassistent.amsterdam.ui.theme.Dimens

/** License plate + visitor name (iOS `VisitorView`). */
@Composable
fun VisitorView(visitor: Visitor, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        LicensePlate(visitor.license)
        Text(
            text = visitor.name ?: "",
            style = AppType.name,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = Dimens.paddingNormal),
        )
    }
}
