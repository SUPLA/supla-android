package org.supla.android.di.entrypoints

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.supla.android.data.source.ChannelRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ChannelRepositoryEntryPoint {
  fun provide(): ChannelRepository
}