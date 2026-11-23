package com.eeseka.lynk.core.data.di

import com.eeseka.lynk.core.data.assistant.InMemoryAssistantMessageCountRepository
import com.eeseka.lynk.core.data.assistant.InMemoryDiscoverChatRepository
import com.eeseka.lynk.core.data.assistant.KtorAssistantService
import com.eeseka.lynk.core.data.auth.DataStoreSessionStorage
import com.eeseka.lynk.core.data.auth.KtorAuthService
import com.eeseka.lynk.core.data.hangout.InMemoryHangoutDetailRepository
import com.eeseka.lynk.core.data.hangout.InMemoryHangoutPhotoStatsRepository
import com.eeseka.lynk.core.data.hangout.KtorHangoutNoteService
import com.eeseka.lynk.core.data.hangout.KtorHangoutParticipantService
import com.eeseka.lynk.core.data.hangout.KtorHangoutPhotoService
import com.eeseka.lynk.core.data.hangout.KtorHangoutService
import com.eeseka.lynk.core.data.hangout.SignedUrlHangoutPhotoRepository
import com.eeseka.lynk.core.data.lobby.InMemoryBallotRepository
import com.eeseka.lynk.core.data.lobby.WebSocketLobbyConnectionClient
import com.eeseka.lynk.core.data.lobby.WebSocketLobbyService
import com.eeseka.lynk.core.data.lobby.network.ConnectionRetryHandler
import com.eeseka.lynk.core.data.lobby.network.KtorLobbyWebSocketConnector
import com.eeseka.lynk.core.data.logging.KermitLogger
import com.eeseka.lynk.core.data.media.NativeImageCompressionService
import com.eeseka.lynk.core.data.networking.HttpClientFactory
import com.eeseka.lynk.core.data.notification.InMemoryUnreadNotificationCounter
import com.eeseka.lynk.core.data.notification.KtorDeviceTokenService
import com.eeseka.lynk.core.data.notification.KtorNotificationService
import com.eeseka.lynk.core.data.payment.KtorPaymentService
import com.eeseka.lynk.core.data.profile.KtorUserService
import com.eeseka.lynk.core.data.location.DataStoreLastKnownLocationStorage
import com.eeseka.lynk.core.data.onboarding.DataStoreOnboardingStorage
import com.eeseka.lynk.core.data.settings.DataStoreAppPreferences
import com.eeseka.lynk.core.data.spot.KtorSpotService
import com.eeseka.lynk.core.domain.assistant.AssistantMessageCountRepository
import com.eeseka.lynk.core.domain.assistant.AssistantService
import com.eeseka.lynk.core.domain.assistant.DiscoverChatRepository
import com.eeseka.lynk.core.domain.auth.AuthService
import com.eeseka.lynk.core.domain.auth.SessionStorage
import com.eeseka.lynk.core.domain.hangout.HangoutDetailRepository
import com.eeseka.lynk.core.domain.hangout.HangoutParticipantService
import com.eeseka.lynk.core.domain.hangout.HangoutPhotoRepository
import com.eeseka.lynk.core.domain.hangout.HangoutNoteService
import com.eeseka.lynk.core.domain.hangout.HangoutPhotoService
import com.eeseka.lynk.core.domain.hangout.HangoutPhotoStatsRepository
import com.eeseka.lynk.core.domain.hangout.HangoutService
import com.eeseka.lynk.core.domain.lobby.BallotRepository
import com.eeseka.lynk.core.domain.lobby.LobbyConnectionClient
import com.eeseka.lynk.core.domain.lobby.LobbyService
import com.eeseka.lynk.core.domain.logging.LynkLogger
import com.eeseka.lynk.core.domain.media.ImageCompressionService
import com.eeseka.lynk.core.domain.notification.DeviceTokenService
import com.eeseka.lynk.core.domain.notification.NotificationService
import com.eeseka.lynk.core.domain.notification.UnreadNotificationCounter
import com.eeseka.lynk.core.domain.payment.PaymentService
import com.eeseka.lynk.core.domain.profile.UserService
import com.eeseka.lynk.core.domain.location.LastKnownLocationStorage
import com.eeseka.lynk.core.domain.onboarding.OnboardingStorage
import com.eeseka.lynk.core.domain.settings.AppPreferences
import com.eeseka.lynk.core.domain.spot.SpotService
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformCoreDataModule: Module

val coreDataModule = module {
    includes(platformCoreDataModule)
    single<LynkLogger> { KermitLogger }
    single {
        HttpClientFactory(get(), get()).create(get())
    }
    singleOf(::KtorAuthService) bind AuthService::class
    singleOf(::KtorUserService) bind UserService::class
    singleOf(::DataStoreSessionStorage) bind SessionStorage::class
    singleOf(::DataStoreAppPreferences) bind AppPreferences::class
    singleOf(::DataStoreOnboardingStorage) bind OnboardingStorage::class
    singleOf(::DataStoreLastKnownLocationStorage) bind LastKnownLocationStorage::class
    singleOf(::NativeImageCompressionService) bind ImageCompressionService::class
    singleOf(::KtorSpotService) bind SpotService::class
    singleOf(::KtorHangoutService) bind HangoutService::class
    singleOf(::KtorHangoutParticipantService) bind HangoutParticipantService::class
    singleOf(::KtorHangoutPhotoService) bind HangoutPhotoService::class
    singleOf(::KtorHangoutNoteService) bind HangoutNoteService::class
    singleOf(::SignedUrlHangoutPhotoRepository) bind HangoutPhotoRepository::class
    singleOf(::KtorPaymentService) bind PaymentService::class
    singleOf(::KtorDeviceTokenService) bind DeviceTokenService::class
    singleOf(::KtorNotificationService) bind NotificationService::class
    singleOf(::InMemoryUnreadNotificationCounter) bind UnreadNotificationCounter::class
    singleOf(::InMemoryHangoutDetailRepository) bind HangoutDetailRepository::class
    singleOf(::InMemoryHangoutPhotoStatsRepository) bind HangoutPhotoStatsRepository::class
    singleOf(::KtorAssistantService) bind AssistantService::class
    singleOf(::InMemoryAssistantMessageCountRepository) bind AssistantMessageCountRepository::class
    singleOf(::InMemoryDiscoverChatRepository) bind DiscoverChatRepository::class
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
    singleOf(::ConnectionRetryHandler)
    singleOf(::KtorLobbyWebSocketConnector)
    singleOf(::WebSocketLobbyConnectionClient) bind LobbyConnectionClient::class
    singleOf(::WebSocketLobbyService) bind LobbyService::class
    singleOf(::InMemoryBallotRepository) bind BallotRepository::class
}