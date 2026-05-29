package nl.parkeerassistent.amsterdam.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import nl.parkeerassistent.amsterdam.R

/** Posts a parking notification when an alarm scheduled by [ParkingNotifications] fires. */
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_ID, 0)
        val subtitle = intent.getStringExtra(EXTRA_SUBTITLE).orEmpty()
        val titleRes = when (intent.getStringExtra(EXTRA_TYPE)) {
            TYPE_START -> R.string.notification_start_title
            TYPE_END -> R.string.notification_end_title
            else -> R.string.notification_reminder_title
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, ParkingNotifications.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_logo)
            .setContentTitle(context.getString(titleRes))
            .setContentText(subtitle)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_TYPE = "type"
        const val EXTRA_SUBTITLE = "subtitle"
        const val TYPE_START = "start"
        const val TYPE_END = "end"
        const val TYPE_REMINDER = "reminder"
    }
}
