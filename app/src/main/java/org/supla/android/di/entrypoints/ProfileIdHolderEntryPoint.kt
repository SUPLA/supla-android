package org.supla.android.di.entrypoints

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.supla.android.profile.ProfileIdHolder

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ProfileIdHolderEntryPoint {
    fun provideProfileIdHolder(): ProfileIdHolder
}