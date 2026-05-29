package org.supla.android.extensions

import android.appwidget.AppWidgetManager
import androidx.work.Worker
import dagger.hilt.android.EntryPointAccessors
import org.supla.android.core.infrastructure.suplaclient.SingleCallProvider
import org.supla.android.di.entrypoints.AppWidgetManagerEntryPoint
import org.supla.android.di.entrypoints.SingleCallProviderEntryPoint
import org.supla.android.di.entrypoints.WidgetPreferencesEntryPoint
import org.supla.android.widget.WidgetPreferences

fun Worker.getSingleCallProvider(): SingleCallProvider =
  EntryPointAccessors.fromApplication(
    applicationContext,
    SingleCallProviderEntryPoint::class.java
  ).provideSingleCallProvider()

fun Worker.getWidgetPreferences(): WidgetPreferences =
  EntryPointAccessors.fromApplication(
    applicationContext,
    WidgetPreferencesEntryPoint::class.java
  ).provideWidgetPreferences()

fun Worker.getAppWidgetManager(): AppWidgetManager =
  EntryPointAccessors.fromApplication(
    applicationContext,
    AppWidgetManagerEntryPoint::class.java
  ).provideAppWidgetManager()
