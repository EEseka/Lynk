package com.eeseka.lynk.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.eeseka.lynk.core.data.lobby.lifecycle.NativeAppLifecycleObserver
import com.eeseka.lynk.core.domain.lifecycle.AppLifecycleObserver
import com.eeseka.lynk.core.data.lobby.network.ConnectionErrorHandler
import com.eeseka.lynk.core.data.lobby.network.ConnectivityObserver
import com.eeseka.lynk.core.data.media.ImageCompressor
import com.eeseka.lynk.core.data.notification.FirebasePushNotificationService
import com.eeseka.lynk.core.data.util.createDataStore
import com.eeseka.lynk.core.domain.notification.PushNotificationService
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformCoreDataModule = module {
    single<HttpClientEngine> { Darwin.create() }
    single<DataStore<Preferences>> {
        createDataStore()
    }
    singleOf(::ImageCompressor)
    singleOf(::NativeAppLifecycleObserver) bind AppLifecycleObserver::class
    singleOf(::ConnectivityObserver)
    singleOf(::ConnectionErrorHandler)
    singleOf(::FirebasePushNotificationService) bind PushNotificationService::class
}