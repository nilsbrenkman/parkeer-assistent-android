package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.auth.BiometricResult
import nl.parkeerassistent.amsterdam.auth.Biometrics
import nl.parkeerassistent.amsterdam.data.local.CredentialStore
import nl.parkeerassistent.amsterdam.data.model.Credentials
import nl.parkeerassistent.amsterdam.ui.account.AccountViewModel
import nl.parkeerassistent.amsterdam.ui.components.ButtonWait
import nl.parkeerassistent.amsterdam.ui.components.SectionHeader
import nl.parkeerassistent.amsterdam.ui.session.SessionViewModel
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.Dimens
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import nl.parkeerassistent.amsterdam.util.findActivity
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * Login form (iOS `LoginView`). On launch, if there are saved credentials it prompts biometrics to
 * unlock them, prefills the most-recent account, and auto-logs-in (once) when enabled. With saved
 * accounts it shows an account picker; otherwise a "remember" toggle (when biometrics are available).
 */
@Composable
fun LoginScreen(
    session: SessionViewModel = koinViewModel(),
    accountVm: AccountViewModel = koinViewModel(),
    credentialStore: CredentialStore = koinInject(),
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var storeCredentials by remember { mutableStateOf(false) }
    var wait by remember { mutableStateOf(false) }
    var authenticationFailed by remember { mutableStateOf(false) }

    val accounts by accountVm.accounts.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val canAuthenticate = remember { Biometrics.isAvailable(context) }
    val scope = rememberCoroutineScope()

    val promptTitle = stringResource(R.string.login_login)
    val promptSubtitle = stringResource(R.string.login_reason)

    fun login() {
        if (!wait) {
            wait = true
            session.login(username, password, storeCredentials = storeCredentials)
            wait = false
        }
    }

    suspend fun authenticate() {
        val stored = credentialStore.retrieve()
        if (stored.isEmpty()) return

        if (!accountVm.isAuthenticated()) {
            val activity = context.findActivity() as? FragmentActivity
            when (activity?.let { Biometrics.authenticate(it, promptTitle, promptSubtitle) } ?: BiometricResult.Unavailable) {
                BiometricResult.Success -> accountVm.onAuthenticated()
                BiometricResult.Failed -> { authenticationFailed = true; return }
                BiometricResult.Unavailable -> return
            }
        }

        val account = stored.firstOrNull { it.username == credentialStore.recent } ?: stored.first()
        if (username.isEmpty()) {
            username = account.username
            password = account.password
        }
        if (session.consumeAutoLogin() && credentialStore.autoLoginEnabled) login()
    }

    LaunchedEffect(Unit) { authenticate() }

    LoginContent(
        username = username,
        password = password,
        storeCredentials = storeCredentials,
        wait = wait,
        accounts = accounts,
        canAuthenticate = canAuthenticate,
        authenticationFailed = authenticationFailed,
        onUsernameChange = { username = it },
        onPasswordChange = { password = it },
        onStoreCredentialsChange = { storeCredentials = it },
        onSelectAccount = { account ->
            username = account.username
            password = account.password
            credentialStore.recent = account.username
        },
        onRetryBiometric = {
            authenticationFailed = false
            scope.launch { authenticate() }
        },
        onLogin = ::login,
    )
}

@Composable
internal fun LoginContent(
    username: String,
    password: String,
    storeCredentials: Boolean,
    wait: Boolean,
    accounts: List<Credentials>,
    canAuthenticate: Boolean,
    authenticationFailed: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onStoreCredentialsChange: (Boolean) -> Unit,
    onSelectAccount: (Credentials) -> Unit,
    onRetryBiometric: () -> Unit,
    onLogin: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        HeaderView(loggedIn = false)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.paddingNormal),
        ) {
            SectionHeader(stringResource(R.string.login_login), Modifier.padding(vertical = Dimens.paddingNormal))
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text(stringResource(R.string.login_username)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.login_password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().padding(top = Dimens.paddingSmall),
            )

            when {
                accounts.isNotEmpty() -> AccountPicker(accounts, username, onSelectAccount)
                authenticationFailed -> OutlinedButton(
                    onClick = onRetryBiometric,
                    modifier = Modifier.fillMaxWidth().padding(top = Dimens.paddingSmall),
                ) { Text(stringResource(R.string.account_unlock)) }
                canAuthenticate -> Row(
                    modifier = Modifier.fillMaxWidth().padding(top = Dimens.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = storeCredentials, onCheckedChange = onStoreCredentialsChange)
                    Text(stringResource(R.string.login_remember_short))
                }
            }

            Button(
                onClick = onLogin,
                enabled = username.isNotEmpty() && password.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.success,
                    contentColor = AppTheme.colors.enabled,
                ),
                modifier = Modifier.fillMaxWidth().padding(top = Dimens.paddingNormal),
            ) {
                ButtonWait(wait) { Text(stringResource(R.string.login_login)) }
            }
        }
    }
}

@Composable
private fun AccountPicker(accounts: List<Credentials>, selectedUsername: String, onSelect: (Credentials) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val current = accounts.firstOrNull { it.username == selectedUsername }
    Box(Modifier.padding(top = Dimens.paddingSmall)) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(current?.let { it.alias ?: it.username } ?: stringResource(R.string.account_label))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.alias ?: account.username) },
                    onClick = { expanded = false; onSelect(account) },
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 500)
@Composable
private fun LoginPreview() = ParkeerAssistentTheme {
    LoginContent(
        username = "test",
        password = "1234",
        storeCredentials = true,
        wait = false,
        accounts = emptyList(),
        canAuthenticate = true,
        authenticationFailed = false,
        onUsernameChange = {},
        onPasswordChange = {},
        onStoreCredentialsChange = {},
        onSelectAccount = {},
        onRetryBiometric = {},
        onLogin = {},
    )
}
