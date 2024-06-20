package org.supla.android.di.entrypoints

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.supla.android.events.UpdateEventsManager

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UpdateEventsManagerEntryPoint {
  fun provideUpdateEventsManager(): UpdateEventsManager
}
