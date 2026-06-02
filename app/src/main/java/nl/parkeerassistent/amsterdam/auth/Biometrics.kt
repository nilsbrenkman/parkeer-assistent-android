package nl.parkeerassistent.amsterdam.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import nl.parkeerassistent.amsterdam.R
import kotlin.coroutines.resume

/** Outcome of a biometric check, mirroring the iOS `AuthenticationError` cases. */
enum class BiometricResult { Success, Failed, Unavailable }

/**
 * Gates access to saved credentials behind device biometrics (iOS uses `LAContext`). On devices
 * without enrolled biometrics this reports [BiometricResult.Unavailable] and the saved-accounts
 * feature is simply not offered — manual login still works.
 */
object Biometrics {

    fun isAvailable(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

    suspend fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
    ): BiometricResult {
        if (!isAvailable(activity)) return BiometricResult.Unavailable

        return suspendCancellableCoroutine { cont ->
            val prompt = BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(activity),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (cont.isActive) cont.resume(BiometricResult.Success)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        // Includes user cancel / negative button — treat as failed (no retry).
                        if (cont.isActive) cont.resume(BiometricResult.Failed)
                    }
                    // onAuthenticationFailed (a single bad read) is intentionally ignored so the
                    // user can retry within the same prompt.
                },
            )
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(activity.getString(R.string.common_cancel))
                .setAllowedAuthenticators(BIOMETRIC_WEAK)
                .build()
            prompt.authenticate(info)
        }
    }
}
