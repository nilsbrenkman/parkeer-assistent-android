package nl.parkeerassistent.amsterdam.di

import nl.parkeerassistent.amsterdam.data.remote.GeoApi
import nl.parkeerassistent.amsterdam.data.remote.LoginApi
import nl.parkeerassistent.amsterdam.data.remote.ParkingApi
import nl.parkeerassistent.amsterdam.data.remote.PaymentApi
import nl.parkeerassistent.amsterdam.data.remote.UserApi
import nl.parkeerassistent.amsterdam.data.remote.VisitorApi
import nl.parkeerassistent.amsterdam.data.repository.GeoRepository
import nl.parkeerassistent.amsterdam.data.repository.GeoRepositoryImpl
import nl.parkeerassistent.amsterdam.data.repository.LoginRepository
import nl.parkeerassistent.amsterdam.data.repository.LoginRepositoryImpl
import nl.parkeerassistent.amsterdam.data.repository.ParkingRepository
import nl.parkeerassistent.amsterdam.data.repository.ParkingRepositoryImpl
import nl.parkeerassistent.amsterdam.data.repository.PaymentRepository
import nl.parkeerassistent.amsterdam.data.repository.PaymentRepositoryImpl
import nl.parkeerassistent.amsterdam.data.repository.UserRepository
import nl.parkeerassistent.amsterdam.data.repository.UserRepositoryImpl
import nl.parkeerassistent.amsterdam.data.repository.VisitorRepository
import nl.parkeerassistent.amsterdam.data.repository.VisitorRepositoryImpl
import nl.parkeerassistent.amsterdam.data.local.CredentialStore
import nl.parkeerassistent.amsterdam.data.local.EncryptedCredentialStore
import nl.parkeerassistent.amsterdam.location.LocationProvider
import nl.parkeerassistent.amsterdam.notifications.AlarmParkingNotifications
import nl.parkeerassistent.amsterdam.notifications.NotificationSettings
import nl.parkeerassistent.amsterdam.notifications.ParkingNotifications
import nl.parkeerassistent.amsterdam.review.AppReview
import nl.parkeerassistent.amsterdam.stats.PrefsStatsStore
import nl.parkeerassistent.amsterdam.stats.StatsStore
import nl.parkeerassistent.amsterdam.util.AndroidStringProvider
import nl.parkeerassistent.amsterdam.util.StringProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create

val dataModule = module {

    // Local persistence + resources
    single<CredentialStore> { EncryptedCredentialStore(androidContext()) }
    single<StringProvider> { AndroidStringProvider(androidContext()) }

    // Notifications
    single { NotificationSettings(androidContext()) }
    single<ParkingNotifications> { AlarmParkingNotifications(androidContext(), get()) }

    // Stats + in-app review
    single<StatsStore> { PrefsStatsStore(androidContext()) }
    single { AppReview(androidContext()) }

    // Location
    single { LocationProvider(androidContext()) }

    // Retrofit service interfaces
    single { get<Retrofit>().create<LoginApi>() }
    single { get<Retrofit>().create<UserApi>() }
    single { get<Retrofit>().create<VisitorApi>() }
    single { get<Retrofit>().create<ParkingApi>() }
    single { get<Retrofit>().create<PaymentApi>() }
    single { get<Retrofit>().create<GeoApi>() }

    // Repositories
    single<LoginRepository> { LoginRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<VisitorRepository> { VisitorRepositoryImpl(get()) }
    single<ParkingRepository> { ParkingRepositoryImpl(get()) }
    single<PaymentRepository> { PaymentRepositoryImpl(get()) }
    single<GeoRepository> { GeoRepositoryImpl(get()) }
}
