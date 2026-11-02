package org.supla.android.di.entrypoints

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.supla.android.core.infrastructure.suplaclient.SingleCallProvider

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SingleCallProviderEntryPoint {
  fun provideSingleCallProvider(): SingleCallProvider
}
