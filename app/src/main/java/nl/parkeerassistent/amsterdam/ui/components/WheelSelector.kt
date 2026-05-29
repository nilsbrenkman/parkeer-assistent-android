package nl.parkeerassistent.amsterdam.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Rotary minute selector (iOS `WheelSelector`). Dragging the dial reports the *signed change* in
 * minutes since the last emission via [onChange]; callers accumulate it. The angle→value math is
 * ported directly from iOS (60 units per full turn, with a non-linear acceleration on fast spins).
 */
@Composable
fun WheelSelector(
    modifier: Modifier = Modifier,
    radius: Dp = 50.dp,
    thickness: Dp = 12.dp,
    onChange: (Int) -> Unit,
) {
    val density = LocalDensity.current
    val radiusPx = with(density) { radius.toPx() }
    val sizePx = with(density) { thickness.toPx() }

    var angle by remember { mutableFloatStateOf(0f) }
    var angleBegin by remember { mutableFloatStateOf(0f) }
    var anglePrev by remember { mutableFloatStateOf(0f) }
    var valuePrev by remember { mutableIntStateOf(0) }

    val grey70 = AppTheme.colors.grey70
    val grey80 = AppTheme.colors.grey80
    val grey90 = AppTheme.colors.grey90

    Canvas(
        modifier = modifier
            .size(radius * 2)
            .pointerInput(Unit) {
                val center = Offset(radiusPx, radiusPx)
                fun degree(o: Offset): Float =
                    (atan2((o.y - center.y).toDouble(), (o.x - center.x).toDouble()) * 180.0 / Math.PI).toFloat()

                var start = Offset.Zero
                detectDragGestures(
                    onDragStart = { start = it },
                    onDragEnd = {
                        angleBegin = angle
                        anglePrev = 0f
                        valuePrev = 0
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val angleStart = degree(start)
                        val angleLocation = degree(change.position)
                        var diff = (angleLocation - angleStart - anglePrev) % 360f
                        if (diff > 180f) diff -= 360f else if (diff < -180f) diff += 360f

                        anglePrev += diff
                        angle = angleBegin + anglePrev

                        val valueNew = (anglePrev / 360f * 60f).toInt()
                        if (valueNew != valuePrev) {
                            val valueDiff = valueNew - valuePrev
                            valuePrev = valueNew
                            onChange((valueDiff.toDouble() * sqrt(abs(valueDiff.toDouble()))).toInt())
                        }
                    },
                )
            },
    ) {
        val c = this.center
        val ringRadius = radiusPx - sizePx

        // Outer solid ring.
        drawCircle(color = grey80, radius = ringRadius, style = Stroke(width = sizePx * 2), center = c)
        // Dashed ring that rotates with the dial.
        rotate(degrees = angle, pivot = c) {
            drawCircle(
                color = grey90,
                radius = ringRadius,
                style = Stroke(width = sizePx, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 4.85f))),
                center = c,
            )
        }
        // Inner detail rings + filled hub (iOS getFrameSize offsets -1, -3, -4).
        drawCircle(color = grey70, radius = ringRadius - 1f, style = Stroke(width = 2f), center = c)
        drawCircle(color = grey80, radius = ringRadius - 3f, style = Stroke(width = 2f), center = c)
        drawCircle(color = grey70, radius = ringRadius - 4f, center = c)
    }
}
