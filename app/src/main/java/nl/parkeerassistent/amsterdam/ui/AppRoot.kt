package nl.parkeerassistent.amsterdam.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.parkeerassistent.amsterdam.ui.common.MessageBus
import nl.parkeerassistent.amsterdam.ui.navigation.AppNavHost
import nl.parkeerassistent.amsterdam.ui.screen.LoadingScreen
import nl.parkeerassistent.amsterdam.ui.screen.LoginScreen
import nl.parkeerassistent.amsterdam.ui.session.SessionViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * App root (iOS `ContentView`). Chooses Loading / Login / main nav from session state, re-checks
 * login on return from background, and shows [MessageBus] events as snackbars.
 */
@Composable
fun AppRoot(
    session: SessionViewModel = koinViewModel(),
    messageBus: MessageBus = koinInject(),
) {
    val state by session.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { session.checkLoggedIn() }

    // Request POST_NOTIFICATIONS once on Android 13+ so parking alarms can be shown.
    val context = LocalContext.current
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* result ignored — notifications are best-effort */ }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> session.onEnterBackground()
                Lifecycle.Event.ON_START -> session.onReturnFromBackground()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(messageBus) {
        messageBus.messages.collect { snackbarHostState.showSnackbar(it.text) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading || state.isBackground -> LoadingScreen()
                state.isLoggedIn -> AppNavHost(onLogout = { session.logout() })
                else -> LoginScreen()
            }
        }
    }
}
