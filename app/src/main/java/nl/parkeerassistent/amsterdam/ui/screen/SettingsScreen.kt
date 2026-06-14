package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.data.local.CredentialStore
import nl.parkeerassistent.amsterdam.notifications.NotificationSettings
import nl.parkeerassistent.amsterdam.ui.components.SectionHeader
import nl.parkeerassistent.amsterdam.ui.components.TitleBar
import nl.parkeerassistent.amsterdam.ui.theme.Dimens
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import org.koin.compose.koinInject

private val INTERVAL_LABELS = listOf("15 m", "30 m", "1 u", "2 u", "3 u", "4 u")

/**
 * Settings (iOS `SettingsView`). Auto-login is persisted via [CredentialStore]; the notification
 * toggles are still in-memory (their scheduling lands with the notifications sub-chunk of Phase 7).
 */
@Composable
fun SettingsScreen(
    credentialStore: CredentialStore = koinInject(),
    notificationSettings: NotificationSettings = koinInject(),
) {
    var onStart by remember { mutableStateOf(notificationSettings.onStart) }
    var onStop by remember { mutableStateOf(notificationSettings.onStop) }
    var reminders by remember { mutableStateOf(notificationSettings.reminders) }
    var interval by remember { mutableFloatStateOf(notificationSettings.intervalIndex.toFloat()) }
    var autoLogin by remember { mutableStateOf(credentialStore.autoLoginEnabled) }

    SettingsContent(
        onStart = onStart,
        onStop = onStop,
        reminders = reminders,
        interval = interval,
        autoLogin = autoLogin,
        onStartChange = { onStart = it; notificationSettings.onStart = it },
        onStopChange = { onStop = it; notificationSettings.onStop = it },
        onRemindersChange = { reminders = it; notificationSettings.reminders = it },
        onIntervalChange = { interval = it; notificationSettings.intervalIndex = it.toInt() },
        onAutoLoginChange = {
            autoLogin = it
            credentialStore.autoLoginEnabled = it
        },
    )
}

@Composable
internal fun SettingsContent(
    onStart: Boolean,
    onStop: Boolean,
    reminders: Boolean,
    interval: Float,
    autoLogin: Boolean,
    onStartChange: (Boolean) -> Unit,
    onStopChange: (Boolean) -> Unit,
    onRemindersChange: (Boolean) -> Unit,
    onIntervalChange: (Float) -> Unit,
    onAutoLoginChange: (Boolean) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TitleBar(title = stringResource(R.string.settings_header))
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.contentPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            SectionHeader(stringResource(R.string.settings_notifications))
            ToggleRow(stringResource(R.string.settings_on_start), onStart, onStartChange)
            ToggleRow(stringResource(R.string.settings_on_stop), onStop, onStopChange)
            ToggleRow(stringResource(R.string.settings_reminders), reminders, onRemindersChange)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = interval,
                    onValueChange = onIntervalChange,
                    valueRange = 0f..5f,
                    steps = 4,
                    enabled = reminders,
                    modifier = Modifier.weight(1f),
                )
                Text(INTERVAL_LABELS[interval.toInt()], Modifier.width(Dimens.buttonHeight))
            }

            SectionHeader(stringResource(R.string.login_login), Modifier.padding(top = Dimens.paddingNormal))
            ToggleRow(stringResource(R.string.account_auto_login), autoLogin, onAutoLoginChange)
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.paddingNano),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
private fun SettingsPreview() = ParkeerAssistentTheme {
    SettingsContent(true, true, true, 1f, true, {}, {}, {}, {}, {})
}
