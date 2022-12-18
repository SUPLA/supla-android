package org.supla.android.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.supla.android.data.ValuesFormatter

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ValuesFormatterEntryPoint {
  fun provideValuesFormatter(): ValuesFormatter
}
