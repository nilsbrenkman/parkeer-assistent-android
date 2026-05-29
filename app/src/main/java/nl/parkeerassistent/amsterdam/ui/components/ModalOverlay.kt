package nl.parkeerassistent.amsterdam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Dimmed full-screen overlay with centered content (iOS `Modal` modifier). Place at the top of a
 * screen's root `Box`; tapping the scrim invokes [onClose]. Tapping the content does nothing.
 */
@Composable
fun ModalOverlay(
    visible: Boolean,
    onClose: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    if (!visible) return
    val scrimInteraction = remember { MutableInteractionSource() }
    val contentInteraction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray.copy(alpha = 0.5f))
            .clickable(interactionSource = scrimInteraction, indication = null) { onClose?.invoke() },
        contentAlignment = Alignment.Center,
    ) {
        // Absorb taps so they don't reach the scrim and dismiss the modal.
        Box(
            modifier = Modifier.clickable(interactionSource = contentInteraction, indication = null) {},
            content = content,
        )
    }
}
