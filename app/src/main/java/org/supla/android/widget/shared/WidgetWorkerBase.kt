package org.supla.android.widget.shared

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.supla.android.extensions.getSingleCallProvider
import org.supla.android.extensions.getWidgetPreferences
import org.supla.android.lib.singlecall.ChannelValue
import org.supla.android.widget.WidgetConfiguration

abstract class WidgetWorkerBase(appContext: Context, workerParams: WorkerParameters) :
  Worker(appContext, workerParams) {

  protected val preferences = getWidgetPreferences()
  protected val singleCallProvider = getSingleCallProvider()

  protected fun loadValue(configuration: WidgetConfiguration): ChannelValue =
    singleCallProvider.provide(configuration.profileId)
      .getChannelValue(configuration.itemId)

  protected fun updateWidgetConfiguration(widgetId: Int, configuration: WidgetConfiguration) =
    preferences.setWidgetConfiguration(widgetId, configuration)
}
