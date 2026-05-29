package nl.parkeerassistent.amsterdam.util

import android.content.Context
import androidx.annotation.StringRes

/** Resolves string resources for non-composable callers (ViewModels, error handler). */
class StringProvider(private val context: Context) {
    fun get(@StringRes resId: Int): String = context.getString(resId)
}
