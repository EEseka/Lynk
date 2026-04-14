package com.eeseka.lynk.shared.data.di

import com.eeseka.lynk.shared.data.auth.DataStoreSessionStorage
import com.eeseka.lynk.shared.data.auth.KtorAuthService
import com.eeseka.lynk.shared.data.logging.KermitLogger
import com.eeseka.lynk.shared.data.media.NativeImageCompressionService
import com.eeseka.lynk.shared.data.networking.HttpClientFactory
import com.eeseka.lynk.shared.data.profile.KtorUserService
import com.eeseka.lynk.shared.data.settings.DataStoreAppPreferences
import com.eeseka.lynk.shared.domain.auth.AuthService
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.logging.LynkLogger
import com.eeseka.lynk.shared.domain.media.ImageCompressionService
import com.eeseka.lynk.shared.domain.profile.UserService
import com.eeseka.lynk.shared.domain.settings.AppPreferences
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformSharedDataModule: Module

val sharedDataModule = module {
    includes(platformSharedDataModule)
    single<LynkLogger> { KermitLogger }
    single {
        HttpClientFactory(get(), get()).create(get())
    }
    singleOf(::KtorAuthService) bind AuthService::class
    singleOf(::KtorUserService) bind UserService::class
    singleOf(::DataStoreSessionStorage) bind SessionStorage::class
    singleOf(::DataStoreAppPreferences) bind AppPreferences::class
    singleOf(::NativeImageCompressionService) bind ImageCompressionService::class
}