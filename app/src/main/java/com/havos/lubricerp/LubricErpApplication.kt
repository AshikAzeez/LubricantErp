package com.havos.lubricerp

import android.app.Application
import com.havos.lubricerp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LubricErpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LubricErpApplication)
            modules(appModule)
        }
    }
}
