package com.met.vetero.di

import com.met.vetero.data.repository.WeatherRepository
import com.met.vetero.presentation.ForecastAdapter
import com.met.vetero.presentation.MainActivityViewModel
import com.met.vetero.presentation.SearchAdapter
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    factory { WeatherRepository(get()) }

    factory { ForecastAdapter() }

    factory { SearchAdapter() }

    viewModel { MainActivityViewModel(get()) }
}