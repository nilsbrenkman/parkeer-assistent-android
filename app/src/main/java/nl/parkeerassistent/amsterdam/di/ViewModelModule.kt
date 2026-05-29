package nl.parkeerassistent.amsterdam.di

import nl.parkeerassistent.amsterdam.ui.account.AccountViewModel
import nl.parkeerassistent.amsterdam.ui.common.ApiErrorHandler
import nl.parkeerassistent.amsterdam.ui.common.MessageBus
import nl.parkeerassistent.amsterdam.ui.parking.ParkingViewModel
import nl.parkeerassistent.amsterdam.ui.parkingmeter.ParkingMeterViewModel
import nl.parkeerassistent.amsterdam.ui.payment.PaymentViewModel
import nl.parkeerassistent.amsterdam.ui.session.SessionViewModel
import nl.parkeerassistent.amsterdam.ui.user.UserViewModel
import nl.parkeerassistent.amsterdam.ui.visitor.VisitorViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {

    // App-wide singletons
    single { MessageBus() }
    single { ApiErrorHandler(get(), get()) }

    // ViewModels
    viewModelOf(::SessionViewModel)
    viewModelOf(::UserViewModel)
    viewModelOf(::VisitorViewModel)
    viewModelOf(::ParkingViewModel)
    viewModelOf(::PaymentViewModel)
    viewModelOf(::AccountViewModel)
    viewModelOf(::ParkingMeterViewModel)
}
