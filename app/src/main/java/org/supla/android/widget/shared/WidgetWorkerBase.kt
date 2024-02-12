package org.supla.android.widget.shared

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.supla.android.Preferences
import org.supla.android.data.ValuesFormatter
import org.supla.android.extensions.getSingleCallProvider
import org.supla.android.extensions.getWidgetPreferences
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.singlecall.ChannelValue
import org.supla.android.lib.singlecall.TemperatureAndHumidity
import org.supla.android.usecases.channel.valueformatter.ThermometerAndHumidityValueFormatter
import org.supla.android.usecases.channel.valueformatter.ThermometerValueFormatter
import org.supla.android.usecases.channel.valueprovider.ThermometerValueProvider
import org.supla.android.widget.WidgetConfiguration

abstract class WidgetWorkerBase(
  appPreferences: Preferences,
  appContext: Context,
  workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

  protected val preferences = getWidgetPreferences()
  protected val singleCallProvider = getSingleCallProvider()

  private val thermometerValueFormatter = ThermometerValueFormatter(appPreferences)
  private val thermometerAndHumidityValueFormatter = ThermometerAndHumidityValueFormatter(appPreferences)

  protected fun loadValue(configuration: WidgetConfiguration): ChannelValue =
    singleCallProvider.provide(configuration.profileId)
      .getChannelValue(configuration.itemId)

  protected fun updateWidgetConfiguration(widgetId: Int, configuration: WidgetConfiguration) =
    preferences.setWidgetConfiguration(widgetId, configuration)

  protected fun getTemperatureAndHumidityFormatter(
    configuration: WidgetConfiguration,
    withUnit: Boolean
  ): (TemperatureAndHumidity?) -> String {
    return if (configuration.itemFunction == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER) {
      { thermometerValueFormatter.format(it?.temperature ?: ThermometerValueProvider.UNKNOWN_VALUE, withUnit) }
    } else {
      { value -> value?.let { thermometerAndHumidityValueFormatter.format(it, withUnit) } ?: ValuesFormatter.NO_VALUE_TEXT }
    }
  }
}
