package nl.parkeerassistent.amsterdam.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** Unwraps the [Activity] from a (possibly wrapped) Compose [Context], or null. */
fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
