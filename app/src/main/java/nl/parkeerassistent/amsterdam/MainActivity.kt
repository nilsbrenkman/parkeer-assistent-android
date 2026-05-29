package nl.parkeerassistent.amsterdam

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import nl.parkeerassistent.amsterdam.ui.AppRoot
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme

// FragmentActivity (not ComponentActivity) so androidx BiometricPrompt can attach.
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParkeerAssistentTheme {
                AppRoot()
            }
        }
    }
}
