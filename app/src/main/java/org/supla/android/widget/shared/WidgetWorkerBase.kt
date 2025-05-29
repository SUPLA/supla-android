package org.supla.android.widget.shared
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.supla.android.Preferences
import org.supla.android.data.ValuesFormatter
import org.supla.android.extensions.getSingleCallProvider
import org.supla.android.extensions.getWidgetPreferences
import org.supla.android.lib.singlecall.ChannelValue
import org.supla.android.lib.singlecall.TemperatureAndHumidity
import org.supla.android.usecases.channel.valueformatter.ThermometerAndHumidityValueFormatter
import org.supla.android.usecases.channel.valueformatter.ThermometerValueFormatter
import org.supla.android.usecases.channel.valueprovider.ThermometerValueProvider
import org.supla.android.widget.WidgetConfiguration
import org.supla.core.shared.data.model.general.SuplaFunction

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
    return if (configuration.subjectFunction == SuplaFunction.THERMOMETER) {
      { thermometerValueFormatter.format(it?.temperature ?: ThermometerValueProvider.UNKNOWN_VALUE, withUnit) }
    } else {
      { value -> value?.let { thermometerAndHumidityValueFormatter.format(it, withUnit) } ?: ValuesFormatter.NO_VALUE_TEXT }
    }
  }
}
