package com.met.vetero.app

import android.app.Application
import com.google.gson.GsonBuilder
import com.met.vetero.data.api.OpenWeatherApi
import com.met.vetero.data.repository.WeatherRepository
import com.met.vetero.presentation.ForecastAdapter
import com.met.vetero.presentation.MainActivityViewModel
import com.met.vetero.presentation.SearchAdapter
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(listOf(apiModule, appModule))
        }

    }

    private val apiModule = module {
        single<Interceptor> {
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        }

        single {
            OkHttpClient.Builder()
                    .addInterceptor(get<Interceptor>())
                    .build()
        }

        single {
            val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create()

            Retrofit.Builder()
                    .baseUrl("https://api.openweathermap.org/data/2.5/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(get<OkHttpClient>())
                    .build()
        }

        single {
            get<Retrofit>().create(OpenWeatherApi::class.java)
        }
    }

    private val appModule = module {

        factory { WeatherRepository(get()) }

        factory { ForecastAdapter() }

        factory { SearchAdapter() }

        viewModel { MainActivityViewModel(get()) }
    }

}