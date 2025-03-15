package com.partnership.rfid.di

import com.partnership.bjbdocumenttrackerreader.repository.RFIDRepository
import com.partnership.bjbdocumenttrackerreader.repository.RFIDRepositoryImpl
import com.partnership.rfid.repository.UserRepository
import com.partnership.rfid.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Ini harus tetap
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun userRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun RFIDRepository(
        rfidRepositoryImpl: RFIDRepositoryImpl
    ): RFIDRepository
}