package nl.parkeerassistent.amsterdam.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Brand color palette ported from the iOS asset catalog (`Color.ui.*`). Held in a
 * [CompositionLocal] so composables read `AppTheme.colors.danger` etc., independent of the
 * Material color scheme. Only `bw70`, `light` and `licenseBorder` differ between light/dark.
 */
data class AppColors(
    val header: Color,
    val onHeader: Color,
    val license: Color,
    val onLicense: Color,
    val licenseBorder: Color,
    val subSectionHeader: Color,
    val dataBox: Color,
    val light: Color,
    val enabled: Color,
    val success: Color,
    val onSuccess: Color,
    val danger: Color,
    val onDanger: Color,
    val grey70: Color,
    val grey80: Color,
    val grey90: Color,
)

val Header = Color(0xFF007CBC)
val License = Color(0xFFF2BA00)
val Background = Color(0xFFEEEEEE)
val Success = Color(0xFF198754)
val Warning = Color(0xFFF2BA00)
val Danger = Color(0xFFFF2601)
val Grey80 = Color(0xFF808080)
val GreyB2 = Color(0xFFB2B2B2)
val GreyCC = Color(0xFFCCCCCC)
val GreyE6 = Color(0xFFE6E6E6)

private val SharedColors = AppColors(
    header = Header,
    onHeader = Color.White,
    license = License,
    onLicense = Color.Black,
    licenseBorder = Color.Black,
    subSectionHeader = Grey80,
    dataBox = Color.White,
    light = Color.White,
    enabled = Color.White,
    success = Success,
    onSuccess = Color.White,
    danger = Danger,
    onDanger = Color.White,
    grey70 = GreyB2,
    grey80 = GreyCC,
    grey90 = GreyE6,
)

val lightAppColors: AppColors = SharedColors

val darkAppColors: AppColors = SharedColors.copy(
    light = Color(0xFFD9D9D9),
    licenseBorder = Color(0xFFF2BA00),
)

val LocalAppColors = staticCompositionLocalOf { lightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
}
