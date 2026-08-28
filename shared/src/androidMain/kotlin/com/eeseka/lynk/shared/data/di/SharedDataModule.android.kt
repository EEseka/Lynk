package com.eeseka.lynk.shared.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.eeseka.lynk.shared.data.lobby.lifecycle.AppLifecycleObserver
import com.eeseka.lynk.shared.data.lobby.network.ConnectionErrorHandler
import com.eeseka.lynk.shared.data.lobby.network.ConnectivityObserver
import com.eeseka.lynk.shared.data.media.ImageCompressor
import com.eeseka.lynk.shared.data.notification.FirebasePushNotificationService
import com.eeseka.lynk.shared.data.util.createDataStore
import com.eeseka.lynk.shared.domain.notification.PushNotificationService
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformSharedDataModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<DataStore<Preferences>> {
        createDataStore(androidContext())
    }
    singleOf(::ImageCompressor)
    singleOf(::AppLifecycleObserver)
    singleOf(::ConnectivityObserver)
    singleOf(::ConnectionErrorHandler)
    singleOf(::FirebasePushNotificationService) bind PushNotificationService::class
}