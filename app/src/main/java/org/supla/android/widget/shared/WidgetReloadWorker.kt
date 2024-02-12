package org.supla.android.widget.shared

import android.content.ComponentName
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.supla.android.Preferences
import org.supla.android.extensions.getAppWidgetManager
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.singlecall.TemperatureAndHumidity
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.onoff.OnOffWidget
import org.supla.android.widget.onoff.updateOnOffWidget
import org.supla.android.widget.shared.configuration.ItemType
import org.supla.android.widget.single.SingleWidget
import org.supla.android.widget.single.updateSingleWidget

@HiltWorker
class WidgetReloadWorker @AssistedInject constructor(
  appPreferences: Preferences,
  @Assisted context: Context,
  @Assisted workerParameters: WorkerParameters
) : WidgetWorkerBase(appPreferences, context, workerParameters) {

  private val appWidgetManager = getAppWidgetManager()

  override fun doWork(): Result {
    val onOffWidgetIds =
      appWidgetManager.getAppWidgetIds(ComponentName(applicationContext, OnOffWidget::class.java))
    handleWidgets(onOffWidgetIds, true) { context, widgetId ->
      updateOnOffWidget(context, widgetId)
    }
    val singleWidgetIds =
      appWidgetManager.getAppWidgetIds(ComponentName(applicationContext, SingleWidget::class.java))
    handleWidgets(singleWidgetIds, false) { context, widgetId ->
      updateSingleWidget(context, widgetId)
    }

    return Result.success()
  }

  private fun handleWidgets(
    widgetIds: IntArray,
    temperatureWithUnit: Boolean,
    updateWidget: (context: Context, widgetId: Int) -> Unit
  ) {
    for (widgetId in widgetIds) {
      val configuration = preferences.getWidgetConfiguration(widgetId) ?: continue
      if (!isThermometer(configuration)) {
        continue
      }

      val temperature = loadTemperatureAndHumidity(
        { (loadValue(configuration) as TemperatureAndHumidity) },
        getTemperatureAndHumidityFormatter(configuration, temperatureWithUnit)
      )

      updateWidgetConfiguration(widgetId, configuration.copy(value = temperature))
      updateWidget(applicationContext, widgetId)
    }
  }

  private fun isThermometer(configuration: WidgetConfiguration): Boolean =
    configuration.itemType == ItemType.CHANNEL &&
      (
        configuration.itemFunction == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ||
          configuration.itemFunction == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
        )

  companion object {
    internal const val WORK_ID = "SingleWidgetReloadWorker_WorkId"
  }
}
