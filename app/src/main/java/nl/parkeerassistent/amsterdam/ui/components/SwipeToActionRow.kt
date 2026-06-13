package nl.parkeerassistent.amsterdam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import nl.parkeerassistent.amsterdam.ui.theme.AppShape
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.Dimens

/**
 * Full-swipe (end-to-start) row that triggers [onAction] with a red [actionLabel] background.
 * Used for visitor delete, parking stop, and account delete (iOS `swipeActions`).
 */
@Composable
fun SwipeToActionRow(
    actionLabel: String,
    onAction: () -> Unit,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) onAction()
    }
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.danger, AppShape.roundedSmall)
                    .padding(horizontal = Dimens.paddingNormal),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(actionLabel, color = AppTheme.colors.enabled, fontWeight = FontWeight.SemiBold)
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, AppShape.roundedSmall)
                .padding(horizontal = Dimens.paddingSmall),
        ) { content() }
    }
}
