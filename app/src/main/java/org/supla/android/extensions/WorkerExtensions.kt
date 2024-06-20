package org.supla.android.extensions

import android.appwidget.AppWidgetManager
import androidx.work.Worker
import dagger.hilt.android.EntryPointAccessors
import org.supla.android.data.ValuesFormatter
import org.supla.android.di.entrypoints.AppWidgetManagerEntryPoint
import org.supla.android.di.entrypoints.SingleCallProviderEntryPoint
import org.supla.android.di.entrypoints.ValuesFormatterEntryPoint
import org.supla.android.di.entrypoints.WidgetPreferencesEntryPoint
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.widget.WidgetPreferences

fun Worker.getSingleCallProvider(): SingleCall.Provider =
  EntryPointAccessors.fromApplication(
    applicationContext,
    SingleCallProviderEntryPoint::class.java
  ).provideSingleCallProvider()

fun Worker.getValuesFormatter(): ValuesFormatter =
  EntryPointAccessors.fromApplication(
    applicationContext,
    ValuesFormatterEntryPoint::class.java
  ).provideValuesFormatter()

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
