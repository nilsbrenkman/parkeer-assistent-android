package nl.parkeerassistent.amsterdam.util

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import java.util.UUID

/**
 * Values for the `X-ParkeerAssistent-*` analytics headers (port of iOS `addAnalyticHeaders`).
 * [userId] is a stable per-install UUID, generated once and persisted.
 *
 * Extracted as an interface so the analytics-headers interceptor is fakeable on plain JVM (the
 * same testability pattern as the other Context-backed collaborators).
 */
interface DeviceInfo {
    val userId: String
    val osVersion: String
    val appVersion: String
    val appBuild: String
}

/** The production [DeviceInfo], reading the install UUID from prefs and the version from the package. */
class AndroidDeviceInfo(context: Context) : DeviceInfo {

    override val userId: String
    override val osVersion: String = Build.VERSION.RELEASE ?: ""
    override val appVersion: String
    override val appBuild: String

    init {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        userId = prefs.getString(KEY_USER_ID, null)
            ?: UUID.randomUUID().toString().also { prefs.edit { putString(KEY_USER_ID, it) } }

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
