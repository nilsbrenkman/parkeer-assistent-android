package nl.parkeerassistent.amsterdam.util

import android.content.Context
import androidx.annotation.StringRes

/** Resolves string resources for non-composable callers (ViewModels, error handler). */
interface StringProvider {
    fun get(@StringRes resId: Int): String
}

class AndroidStringProvider(private val context: Context) : StringProvider {
    override fun get(@StringRes resId: Int): String = context.getString(resId)
}
