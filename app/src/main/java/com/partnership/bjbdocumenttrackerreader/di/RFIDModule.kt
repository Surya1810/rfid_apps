package com.partnership.rfid.di

import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RFIDModule {

    @Provides
    @Singleton
    fun provideRFIDManager(): RFIDManager {
        return RFIDManager()
    }
}