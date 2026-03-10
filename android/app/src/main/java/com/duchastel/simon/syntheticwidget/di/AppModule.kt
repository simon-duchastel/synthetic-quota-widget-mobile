package com.duchastel.simon.syntheticwidget.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.duchastel.simon.syntheticwidget.data.AuthDataStore
import com.duchastel.simon.syntheticwidget.data.AuthRepository
import com.duchastel.simon.syntheticwidget.data.AuthRepositoryImpl
import com.duchastel.simon.syntheticwidget.data.NetworkClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")
private val Context.quotaDataStore: DataStore<Preferences> by preferencesDataStore(name = "quota_preferences")

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    @Provides
    fun provideNetworkClient(
        httpClient: HttpClient,
        authDataStore: AuthDataStore
    ): NetworkClient {
        return NetworkClient(httpClient, authDataStore)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    fun provideAuthDataStore(
        @ApplicationContext context: Context
    ): AuthDataStore {
        return AuthDataStore(context.authDataStore)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}
