package nl.parkeerassistent.amsterdam.stats

import android.content.Context

/** Usage stats that gate the in-app review prompt (port of iOS `Stats`). */
interface StatsStore {
    fun incrementLogin()
    fun incrementVisitor()
    fun incrementParking()
    fun incrementPayment()
    fun shouldRequestReview(): Boolean
    fun markRequested()
}

/** SharedPreferences-backed stats; `firstLogin` is set when first created. */
class PrefsStatsStore(context: Context) : StatsStore {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    init {
        if (!prefs.contains(KEY_FIRST_LOGIN)) {
            prefs.edit().putLong(KEY_FIRST_LOGIN, System.currentTimeMillis()).apply()
        }
    }

    override fun incrementLogin() = increment(KEY_LOGIN_COUNT)
    override fun incrementVisitor() = increment(KEY_VISITOR_COUNT)
    override fun incrementParking() = increment(KEY_PARKING_COUNT)
    override fun incrementPayment() = increment(KEY_PAYMENT_COUNT)

    /** Port of iOS `Stats.requestReview()`: enough activity, and not asked in the last ~26 weeks. */
    override fun shouldRequestReview(): Boolean {
        val now = System.currentTimeMillis()
        val requested = prefs.getLong(KEY_REQUESTED, 0L)
        if (requested != 0L && now - requested < WEEKS_26_MS) return false
        // Mirrors the iOS check verbatim (firstLogin in the far future ⇒ effectively never true).
        if (prefs.getLong(KEY_FIRST_LOGIN, now) > now + DAYS_14_MS) return false
        if (prefs.getInt(KEY_LOGIN_COUNT, 0) < 10) return false
        val actions = prefs.getInt(KEY_VISITOR_COUNT, 0) +
            prefs.getInt(KEY_PARKING_COUNT, 0) +
            prefs.getInt(KEY_PAYMENT_COUNT, 0)
        return actions >= 10
    }

    override fun markRequested() {
        prefs.edit().putLong(KEY_REQUESTED, System.currentTimeMillis()).apply()
    }

    private fun increment(key: String) {
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
    }

    private companion object {
        const val PREFS = "user_stats"
        const val KEY_FIRST_LOGIN = "firstLogin"
        const val KEY_REQUESTED = "requested"
        const val KEY_LOGIN_COUNT = "loginCount"
        const val KEY_VISITOR_COUNT = "visitorCount"
        const val KEY_PARKING_COUNT = "parkingCount"
        const val KEY_PAYMENT_COUNT = "paymentCount"
        const val WEEKS_26_MS = 26L * 7 * 24 * 60 * 60 * 1000
        const val DAYS_14_MS = 14L * 24 * 60 * 60 * 1000
    }
}
