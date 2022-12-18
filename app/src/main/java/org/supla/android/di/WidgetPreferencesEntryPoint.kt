package org.supla.android.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.supla.android.widget.WidgetPreferences

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetPreferencesEntryPoint {
  fun provideWidgetPreferences(): WidgetPreferences
}
