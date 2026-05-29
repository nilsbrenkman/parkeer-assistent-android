package nl.parkeerassistent.amsterdam.notifications

import android.content.Context

/**
 * Persisted notification preferences (iOS `Notifications` UserDefaults keys). Defaults to enabled
 * (the iOS `@State` defaults are `true`, before being overwritten by unset UserDefaults).
 */
class NotificationSettings(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var onStart: Boolean
        get() = prefs.getBoolean(KEY_START, true)
        set(v) = prefs.edit().putBoolean(KEY_START, v).apply()

    var onStop: Boolean
        get() = prefs.getBoolean(KEY_STOP, true)
        set(v) = prefs.edit().putBoolean(KEY_STOP, v).apply()

    var reminders: Boolean
        get() = prefs.getBoolean(KEY_REMINDER, true)
        set(v) = prefs.edit().putBoolean(KEY_REMINDER, v).apply()

    /** Index into [INTERVAL_MINUTES]. */
    var intervalIndex: Int
        get() = prefs.getInt(KEY_INTERVAL, 1).coerceIn(0, INTERVAL_MINUTES.lastIndex)
        set(v) = prefs.edit().putInt(KEY_INTERVAL, v).apply()

    val intervalMinutes: Int get() = INTERVAL_MINUTES[intervalIndex]

    companion object {
        val INTERVAL_MINUTES = listOf(15, 30, 60, 120, 180, 240)
        private const val PREFS = "notifications"
        private const val KEY_START = "notifyStart"
        private const val KEY_STOP = "notifyStop"
        private const val KEY_REMINDER = "notifyReminder"
        private const val KEY_INTERVAL = "notifyInterval"
    }
}
