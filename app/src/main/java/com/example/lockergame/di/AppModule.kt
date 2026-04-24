package com.example.lockergame.di

import android.content.Context
import com.example.lockergame.data.settings.DataStoreSettingsRepository
import com.example.lockergame.data.settings.SettingsRepository
import com.example.lockergame.feature.game.AndroidSoundPlayer
import com.example.lockergame.feature.game.SoundPlayer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        repository: DataStoreSettingsRepository,
    ): SettingsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideSoundPlayer(
        @ApplicationContext context: Context,
    ): SoundPlayer = AndroidSoundPlayer(context)
}
