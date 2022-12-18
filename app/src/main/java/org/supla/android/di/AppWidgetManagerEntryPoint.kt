package org.supla.android.di

import android.appwidget.AppWidgetManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppWidgetManagerEntryPoint {
  fun provideAppWidgetManager(): AppWidgetManager
}
