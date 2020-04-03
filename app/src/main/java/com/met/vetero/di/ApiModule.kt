package com.met.vetero.di

import com.google.gson.GsonBuilder
import com.met.vetero.data.api.OpenWeatherApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val apiModule = module {
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
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()

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