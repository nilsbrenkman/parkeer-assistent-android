package nl.parkeerassistent.amsterdam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.AppType
import nl.parkeerassistent.amsterdam.ui.theme.Dimens

/** Yellow Dutch-style license plate (iOS `LicenseView`). */
@Composable
fun LicensePlate(license: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(Dimens.radiusSmall)
    Text(
        text = license,
        style = AppType.license,
        color = AppTheme.colors.license,
        modifier = modifier
            .clip(shape)
            .background(AppTheme.colors.licenseBg)
            .border(1.dp, AppTheme.colors.licenseBorder, shape)
            .padding(horizontal = Dimens.paddingSmall, vertical = Dimens.paddingNano),
    )
}
