package nl.parkeerassistent.amsterdam.di

import kotlinx.serialization.json.Json
import nl.parkeerassistent.amsterdam.BuildConfig
import nl.parkeerassistent.amsterdam.data.remote.AnalyticsHeadersInterceptor
import nl.parkeerassistent.amsterdam.data.remote.ErrorInterceptor
import nl.parkeerassistent.amsterdam.data.remote.cookie.PrefsSessionCookieStore
import nl.parkeerassistent.amsterdam.data.remote.cookie.SessionCookieJar
import nl.parkeerassistent.amsterdam.data.remote.cookie.SessionCookieStore
import nl.parkeerassistent.amsterdam.util.AndroidDeviceInfo
import nl.parkeerassistent.amsterdam.util.DeviceInfo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {

    single<SessionCookieStore> { PrefsSessionCookieStore(androidContext()) }
    single { SessionCookieJar(get()) }
    single<DeviceInfo> { AndroidDeviceInfo(androidContext()) }

    single {
        // Tolerate fields the client doesn't model; omit nulls in request bodies (matches iOS).
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }

    single {
        OkHttpClient.Builder()
            .cookieJar(get<SessionCookieJar>())
            .addInterceptor(AnalyticsHeadersInterceptor(get()))
            .addInterceptor(ErrorInterceptor(get()))
            .apply {
                if (BuildConfig.DEBUG) {
                    addNetworkInterceptor(
                        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY },
                    )
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_BASE_URL)
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
