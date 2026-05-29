package nl.parkeerassistent.amsterdam.util

import android.util.Log as AndroidLog

/** Thin logging facade (port of iOS `util/Log`). */
object Log {
    private const val TAG = "ParkeerAssistent"

    fun debug(message: String) = AndroidLog.d(TAG, message).let {}
    fun warning(message: String) = AndroidLog.w(TAG, message).let {}
    fun error(message: String, e: Throwable? = null) = AndroidLog.e(TAG, message, e).let {}
}
