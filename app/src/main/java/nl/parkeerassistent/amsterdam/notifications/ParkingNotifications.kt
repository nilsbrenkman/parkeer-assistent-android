package nl.parkeerassistent.amsterdam.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import nl.parkeerassistent.amsterdam.data.model.Parking
import nl.parkeerassistent.amsterdam.data.model.ParkingResponse
import nl.parkeerassistent.amsterdam.data.model.Visitor
import nl.parkeerassistent.amsterdam.util.DateUtil
import nl.parkeerassistent.amsterdam.util.LicenseUtil

/**
 * Schedules local parking notifications (port of iOS `Notifications`): start/end/reminder
 * notifications for the current sessions. [visitors] is fed from the visitor list for the subtitle.
 */
interface ParkingNotifications {
    var visitors: List<Visitor>
    fun onParking(response: ParkingResponse)

    companion object {
        const val CHANNEL_ID = "parking"
    }
}

/**
 * Reschedules notifications via [AlarmManager] (cancelling the previous set) on each parking
 * refresh; [NotificationReceiver] posts them. Gated by [NotificationSettings].
 */
class AlarmParkingNotifications(
    private val context: Context,
    private val settings: NotificationSettings,
) : ParkingNotifications {

    /** Set by the visitor list (iOS `Notifications.store.visitors`); used for the subtitle. */
    @Volatile
    override var visitors: List<Visitor> = emptyList()

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    init {
        createChannel()
    }

    override fun onParking(response: ParkingResponse) {
        cancelAll()
        val codes = mutableListOf<Int>()
        response.active.forEach {
            scheduleReminders(it, codes)
            scheduleEnd(it, codes)
        }
        response.scheduled.forEach {
            scheduleStart(it, codes)
            scheduleEnd(it, codes)
            scheduleReminders(it, codes)
        }
        prefs.edit().putString(KEY_CODES, codes.joinToString(",")).apply()
    }

    private fun scheduleStart(parking: Parking, codes: MutableList<Int>) {
        if (!settings.onStart) return
        val at = DateUtil.parseWire(parking.startTime)?.toInstant()?.toEpochMilli() ?: return
        subtitle(parking)?.let { schedule(NotificationReceiver.TYPE_START, it, at, codes) }
    }

    private fun scheduleEnd(parking: Parking, codes: MutableList<Int>) {
        if (!settings.onStop) return
        val at = DateUtil.parseWire(parking.endTime)?.toInstant()?.toEpochMilli() ?: return
        subtitle(parking)?.let { schedule(NotificationReceiver.TYPE_END, it, at, codes) }
    }

    private fun scheduleReminders(parking: Parking, codes: MutableList<Int>) {
        if (!settings.reminders) return
        val start = DateUtil.parseWire(parking.startTime)?.toInstant()?.toEpochMilli() ?: return
        val end = DateUtil.parseWire(parking.endTime)?.toInstant()?.toEpochMilli() ?: return
        val subtitle = subtitle(parking) ?: return
        val intervalMs = settings.intervalMinutes * 60_000L

        var reminder = start + intervalMs
        val now = System.currentTimeMillis()
        while (reminder < now) reminder += intervalMs
        while (reminder < end) {
            schedule(NotificationReceiver.TYPE_REMINDER, subtitle, reminder, codes)
            reminder += intervalMs
        }
    }

    private fun subtitle(parking: Parking): String? {
        val visitor = visitors.firstOrNull { it.license.equals(parking.license, ignoreCase = true) } ?: return null
        val license = LicenseUtil.format(visitor.license)
        return visitor.name?.let { "$it | [ $license ]" } ?: "[ $license ]"
    }

    private fun schedule(type: String, subtitle: String, atMillis: Long, codes: MutableList<Int>) {
        if (atMillis <= System.currentTimeMillis()) return
        val code = nextCode()
        val pending = pendingIntent(code, type, subtitle)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pending)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pending)
        }
        codes.add(code)
    }

    private fun cancelAll() {
        val codes = prefs.getString(KEY_CODES, null)?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        for (code in codes) {
            alarmManager.cancel(pendingIntent(code, NotificationReceiver.TYPE_REMINDER, ""))
        }
        prefs.edit().remove(KEY_CODES).apply()
    }

    private fun pendingIntent(code: Int, type: String, subtitle: String): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.EXTRA_ID, code)
            putExtra(NotificationReceiver.EXTRA_TYPE, type)
            putExtra(NotificationReceiver.EXTRA_SUBTITLE, subtitle)
        }
        return PendingIntent.getBroadcast(
            context,
            code,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun nextCode(): Int {
        val next = prefs.getInt(KEY_NEXT_CODE, 1)
        prefs.edit().putInt(KEY_NEXT_CODE, next + 1).apply()
        return next
    }

    private fun createChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = context.getString(nl.parkeerassistent.amsterdam.R.string.notification_channel_name)
        val channel = NotificationChannel(ParkingNotifications.CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val PREFS = "notif_alarms"
        const val KEY_CODES = "codes"
        const val KEY_NEXT_CODE = "nextCode"
    }
}
