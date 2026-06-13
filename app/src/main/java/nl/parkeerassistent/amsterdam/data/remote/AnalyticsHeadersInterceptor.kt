package nl.parkeerassistent.amsterdam.data.remote

import nl.parkeerassistent.amsterdam.util.DeviceInfo
import okhttp3.Interceptor
import okhttp3.Response

/** Adds the `X-ParkeerAssistent-*` analytics headers to every request (port of iOS). */
class AnalyticsHeadersInterceptor(private val device: DeviceInfo) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("X-ParkeerAssistent-UserId", device.userId)
            .header("X-ParkeerAssistent-OS", "Android")
            .header("X-ParkeerAssistent-SDK", device.osVersion)
            .header("X-ParkeerAssistent-Version", device.appVersion)
            .header("X-ParkeerAssistent-Build", device.appBuild)
//            .header("X-ParkeerAssistent-Mock", "true")
            .build()
        return chain.proceed(request)
    }
}
