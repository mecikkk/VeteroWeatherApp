package com.met.vetero.di

import com.met.vetero.data.repository.WeatherRepository
import com.met.vetero.presentation.MainActivityViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    factory { WeatherRepository(get())}

    viewModel { MainActivityViewModel(get()) }
}