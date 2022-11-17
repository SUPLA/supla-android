package org.supla.android.extensions

import android.appwidget.AppWidgetManager
import androidx.work.Worker
import dagger.hilt.android.EntryPointAccessors
import org.supla.android.data.TemperatureFormatter
import org.supla.android.di.AppWidgetManagerEntryPoint
import org.supla.android.di.SingleCallProviderEntryPoint
import org.supla.android.di.TemperaturePresenterEntryPoint
import org.supla.android.di.WidgetPreferencesEntryPoint
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.widget.WidgetPreferences

fun Worker.getSingleCallProvider(): SingleCall.Provider =
  EntryPointAccessors.fromApplication(
    applicationContext,
    SingleCallProviderEntryPoint::class.java
  ).provideSingleCallProvider()

fun Worker.getTemperatureFormatter(): TemperatureFormatter =
  EntryPointAccessors.fromApplication(
    applicationContext,
    TemperaturePresenterEntryPoint::class.java
  ).provideTemperaturePresenter()

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
