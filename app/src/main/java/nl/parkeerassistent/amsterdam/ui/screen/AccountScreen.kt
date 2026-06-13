package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.auth.BiometricResult
import nl.parkeerassistent.amsterdam.auth.Biometrics
import nl.parkeerassistent.amsterdam.data.model.Credentials
import nl.parkeerassistent.amsterdam.ui.account.AccountViewModel
import nl.parkeerassistent.amsterdam.ui.common.MessageBus
import nl.parkeerassistent.amsterdam.ui.common.MessageType
import nl.parkeerassistent.amsterdam.ui.components.SubSectionHeader
import nl.parkeerassistent.amsterdam.ui.components.SwipeToActionRow
import nl.parkeerassistent.amsterdam.ui.components.TitleBar
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.AppType
import nl.parkeerassistent.amsterdam.ui.theme.Dimens
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import nl.parkeerassistent.amsterdam.util.findActivity
import org.koin.compose.koinInject

/** Saved-accounts list, gated behind a biometric prompt (iOS `AccountView`). */
@Composable
fun AccountsScreen(
    accountVm: AccountViewModel,
    onOpen: (username: String) -> Unit,
    onAdd: () -> Unit,
    onAuthFailed: () -> Unit,
    messageBus: MessageBus = koinInject(),
) {
    val accounts by accountVm.accounts.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() as? FragmentActivity }
    val promptTitle = stringResource(R.string.account_header)
    val promptSubtitle = stringResource(R.string.account_unlock)
    val unavailableMsg = stringResource(R.string.account_biometric_unavailable)

    LaunchedEffect(Unit) {
        if (accountVm.isAuthenticated()) return@LaunchedEffect
        val result = activity?.let {
            Biometrics.authenticate(it, promptTitle, promptSubtitle)
        } ?: BiometricResult.Unavailable
        when (result) {
            BiometricResult.Success -> accountVm.onAuthenticated()
            BiometricResult.Unavailable -> {
                messageBus.show(unavailableMsg, MessageType.ERROR)
                onAuthFailed()
            }
            BiometricResult.Failed -> onAuthFailed()
        }
    }

    AccountsContent(
        accounts = accounts,
        onOpen = onOpen,
        onAdd = onAdd,
        onDelete = { accountVm.deleteAccount(it) },
    )
}

@Composable
private fun AccountsContent(
    accounts: List<Credentials>,
    onOpen: (username: String) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Credentials) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TitleBar(title = stringResource(R.string.account_header))
        LazyColumn(Modifier.weight(1f).padding(horizontal = Dimens.paddingNormal)) {
            if (accounts.isEmpty()) {
                item {
                    HorizontalDivider()
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(vertical = Dimens.paddingNormal)
                            .fillMaxWidth(),
                    ) {
                        SubSectionHeader(stringResource(R.string.account_empty))
                    }
                    HorizontalDivider()
                }
            }
            item { HorizontalDivider() }
            items(accounts, key = { it.username }) { account ->
                SwipeToActionRow(
                    actionLabel = stringResource(R.string.common_delete),
                    onAction = { onDelete(account) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(account.username) }
                            .padding(vertical = Dimens.paddingSmall),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = account.alias ?: account.username,
                            style = AppType.name,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = Dimens.paddingSmall),
                        )
                    }
                }
                HorizontalDivider()
            }
        }
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.success,
                contentColor = AppTheme.colors.enabled,
            ),
            modifier = Modifier.fillMaxWidth().padding(Dimens.paddingNormal),
        ) { Text(stringResource(R.string.common_add)) }
    }
}

/** Add or edit a saved account (iOS `AccountDetailView`). Empty [username] means a new account. */
@Composable
fun AccountDetailScreen(
    username: String,
    accountVm: AccountViewModel,
    onBack: () -> Unit,
) {
    val existing = remember(username) { accountVm.account(username) }
    var alias by remember { mutableStateOf(existing?.alias ?: "") }
    var user by remember { mutableStateOf(existing?.username ?: "") }
    var password by remember { mutableStateOf(existing?.password ?: "") }

    AccountDetailContent(
        isNew = existing == null,
        alias = alias,
        username = user,
        password = password,
        onAliasChange = { alias = it },
        onUsernameChange = { user = it },
        onPasswordChange = { password = it },
        onSave = {
            if (existing == null) {
                accountVm.addAccount(user, password, alias)
            } else {
                accountVm.updateAccount(existing, user, password, alias)
            }
            onBack()
        },
    )
}

@Composable
private fun AccountDetailContent(
    isNew: Boolean,
    alias: String,
    username: String,
    password: String,
    onAliasChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TitleBar(title = stringResource(if (isNew) R.string.account_new_account else R.string.account_details))
        Column(
            Modifier.padding(Dimens.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
        ) {
            OutlinedTextField(alias, onAliasChange, label = { Text(stringResource(R.string.account_alias)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(username, onUsernameChange, label = { Text(stringResource(R.string.login_username)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(password, onPasswordChange, label = { Text(stringResource(R.string.login_password)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = onSave,
                enabled = username.isNotEmpty() && password.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.success,
                    contentColor = AppTheme.colors.enabled,
                ),
                modifier = Modifier.fillMaxWidth().padding(top = Dimens.paddingSmall),
            ) { Text(stringResource(R.string.common_save)) }
        }
    }
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun AccountsContentPreview() = ParkeerAssistentTheme {
    AccountsContent(
        accounts = listOf(
            Credentials("Thuis", "user1", "x"),
            Credentials(null, "user2", "y"),
        ),
        onOpen = {}, onAdd = {}, onDelete = {},
    )
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun AccountDetailContentPreview() = ParkeerAssistentTheme {
    AccountDetailContent(false, "Thuis", "user1", "secret", {}, {}, {}, {})
}
