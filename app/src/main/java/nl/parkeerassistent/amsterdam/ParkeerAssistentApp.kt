package nl.parkeerassistent.amsterdam

import android.app.Application
import nl.parkeerassistent.amsterdam.di.dataModule
import nl.parkeerassistent.amsterdam.di.networkModule
import nl.parkeerassistent.amsterdam.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ParkeerAssistentApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@ParkeerAssistentApp)
            modules(networkModule, dataModule, viewModelModule)
        }
    }
}
