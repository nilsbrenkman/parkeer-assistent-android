package nl.parkeerassistent.amsterdam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.AppType
import nl.parkeerassistent.amsterdam.ui.theme.Dimens
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import nl.parkeerassistent.amsterdam.util.License

/**
 * Yellow Dutch-style license plate (iOS `LicenseView`).
 *
 * Fixed width so every plate renders identically regardless of glyph widths or
 * character count — a plate with narrow/few chars (`1`) is as wide as one with wide chars (`W`).
 */
@Composable
fun LicensePlate(license: String, modifier: Modifier = Modifier) {
    Text(
        text = License.format(license),
        style = AppType.license,
        color = AppTheme.colors.license,
        textAlign = TextAlign.Center,
        modifier = modifier
            .plateBackground()
            .padding(horizontal = Dimens.paddingSmall, vertical = Dimens.paddingSmall),
    )
}

/**
 * Editable counterpart of [LicensePlate] — an in-place text field styled exactly like the yellow
 * plate (same width, background, border and centred [AppType.license] text). `value` is shown via
 * [License.format] grouping, so callers should pass the already-formatted license back in.
 */
@Composable
fun LicensePlateField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = AppType.license.copy(
            color = AppTheme.colors.license,
            textAlign = TextAlign.Center,
        ),
        cursorBrush = SolidColor(AppTheme.colors.license),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        modifier = modifier
            .plateBackground()
            .padding(horizontal = Dimens.paddingSmall, vertical = Dimens.paddingSmall),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.Center) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = AppType.license,
                        color = AppTheme.colors.license.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                    )
                }
                inner()
            }
        },
    )
}

/** Yellow plate background (clip + fill + border + fixed width) shared by plate composables. */
private fun Modifier.plateBackground(): Modifier = composed {
    val shape = RoundedCornerShape(Dimens.radiusSmall)
    clip(shape)
        .background(AppTheme.colors.licenseBg)
        .border(2.dp, AppTheme.colors.licenseBorder, shape)
        .width(Dimens.licenseWidth)
}

@Preview(showBackground = true)
@Composable
private fun LicensePlatePreview() = ParkeerAssistentTheme {
    LicensePlate("12ABC3")
}

@Preview(showBackground = true)
@Composable
private fun LicensePlateFieldPreview() = ParkeerAssistentTheme {
    LicensePlateField(value = "12-ABC-3", onValueChange = {}, placeholder = "00-AA-00")
}
