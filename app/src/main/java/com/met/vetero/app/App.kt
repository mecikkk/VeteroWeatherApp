package com.met.vetero.app

import android.app.Application
import com.met.vetero.di.apiModule
import com.met.vetero.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(listOf(apiModule, appModule))
        }
    }

}