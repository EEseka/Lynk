package com.eeseka.lynk.shared.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.eeseka.lynk.shared.data.util.createDataStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.module

actual val platformSharedDataModule = module {
    single<HttpClientEngine> { Darwin.create() }
    single<DataStore<Preferences>> {
        createDataStore()
    }
}