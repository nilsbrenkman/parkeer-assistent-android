package nl.parkeerassistent.amsterdam.util

import android.content.Context
import android.os.Build
import java.util.UUID

/**
 * Values for the `X-ParkeerAssistent-*` analytics headers (port of iOS `addAnalyticHeaders`).
 * [userId] is a stable per-install UUID, generated once and persisted.
 */
class DeviceInfo(context: Context) {

    val userId: String
    val osVersion: String = Build.VERSION.RELEASE ?: ""
    val appVersion: String
    val appBuild: String

    init {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        userId = prefs.getString(KEY_USER_ID, null)
            ?: UUID.randomUUID().toString().also { prefs.edit().putString(KEY_USER_ID, it).apply() }

        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        appVersion = info.versionName ?: ""
        // minSdk 28 → longVersionCode is always available.
        appBuild = info.longVersionCode.toString()
    }

    private companion object {
        const val PREFS_NAME = "device_info"
        const val KEY_USER_ID = "userId"
    }
}
