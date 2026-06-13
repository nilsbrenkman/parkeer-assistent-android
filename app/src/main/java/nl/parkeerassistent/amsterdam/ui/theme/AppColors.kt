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
    val license: Color,
    val licenseBg: Color,
    val licenseBorder: Color,
    val subSectionHeader: Color,

    val light: Color,
    val bw70: Color,
    val enabled: Color,
    val disabled: Color,
    val info: Color,
    val infoDisabled: Color,
    val success: Color,
    val successDisabled: Color,
    val warning: Color,
    val warningDisabled: Color,
    val danger: Color,
    val dangerDisabled: Color,
    val grey50: Color,
    val grey70: Color,
    val grey80: Color,
    val grey90: Color,
    val focus: Color,
    val focusBg: Color,
    val noFocus: Color,
    val noFocusBg: Color,
)

private val SharedColors = AppColors(
    header = Color(0xFF007CBC),
    license = Color(0xFF000000),
    licenseBg = Color(0xFFF2BA00),
    licenseBorder = Color(0xFF000000),
    subSectionHeader = Color(0xFF808080),
    light = Color(0xFFFFFFFF),
    bw70 = Color(0xFFEEEEEE),
    enabled = Color(0xFFFFFFFF),
    disabled = Color(0xFFDDDDDD),
    info = Color(0xFF007CBC),
    infoDisabled = Color(0xFF6EACCC),
    success = Color(0xFF198754),
    successDisabled = Color(0xFF7BB299),
    warning = Color(0xFFF2BA00),
    warningDisabled = Color(0xFFE8CC6E),
    danger = Color(0xFFFF2601),
    dangerDisabled = Color(0xFFEE826F),
    grey50 = Color(0xFF808080),
    grey70 = Color(0xFFB2B2B2),
    grey80 = Color(0xFFCCCCCC),
    grey90 = Color(0xFFE6E6E6),
    focus = Color(0xFF000000),
    focusBg = Color(0xFFE6E6E6),
    noFocus = Color(0xFF333333),
    noFocusBg = Color(0xFFB2B2B2),
)

val lightAppColors: AppColors = SharedColors

val darkAppColors: AppColors = SharedColors.copy(
    light = Color(0xFFD9D9D9),
    bw70 = Color(0xFF4C4C4C),
    licenseBorder = Color(0xFFF2BA00),
)

val LocalAppColors = staticCompositionLocalOf { lightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
}
