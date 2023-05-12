package org.supla.android.di.entrypoints

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.supla.android.events.ListsEventsManager

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ListsEventsManagerEntryPoint {
  fun provideListsEventsManager(): ListsEventsManager
}