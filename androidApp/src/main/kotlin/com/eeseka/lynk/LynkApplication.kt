package com.eeseka.lynk

import android.app.Application
import com.eeseka.lynk.di.initKoin
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class LynkApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Google Auth
        GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(serverId = AppConfig.WEB_CLIENT_ID)
        )

        // Initialize Koin
        initKoin(config = {
            androidContext(this@LynkApplication)
            androidLogger()
        })
    }
}