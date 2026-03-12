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

import jakarta.inject.Inject
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeBaseConfig
import org.supla.android.data.source.remote.gpm.toValueFormat
import org.supla.android.di.FORMATTER_GPM
import org.supla.android.di.FORMATTER_THERMOMETER
import org.supla.android.di.FORMATTER_THERMOMETER_AND_HUMIDITY
import org.supla.android.lib.singlecall.ChannelValue
import org.supla.android.lib.singlecall.ContainerLevel
import org.supla.android.lib.singlecall.DoubleValue
import org.supla.android.lib.singlecall.ResultException
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.lib.singlecall.TemperatureAndHumidity
import org.supla.android.lib.singlecall.toResult
import org.supla.android.usecases.channel.valueprovider.GpmValueProvider
import org.supla.android.usecases.channelconfig.LoadChannelConfigUseCase
import org.supla.android.widget.WidgetConfiguration
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.formatters.ContainerValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.formatters.HumidityValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import timber.log.Timber
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class WidgetConfigurationUpdater @Inject constructor(
  private val singleCallProvider: SingleCall.Provider,
  private val loadChannelConfigUseCase: LoadChannelConfigUseCase,
  @param:Named(FORMATTER_GPM) private val gpmValueFormatter: ValueFormatter,
  @param:Named(FORMATTER_THERMOMETER) private val thermometerValueFormatter: ValueFormatter,
  @param:Named(FORMATTER_THERMOMETER_AND_HUMIDITY) private val thermometerAndHumidityValueFormatter: ValueFormatter,
) {

  fun update(configuration: WidgetConfiguration, withUnit: Boolean): UpdateResult {
    return try {
      loadValue(configuration, withUnit)?.let { UpdateResult.Success(it) } ?: UpdateResult.Empty
    } catch (ex: ResultException) {
      Timber.w(ex, "Widget value load failed")
      UpdateResult.Error(ex.toResult)
    } catch (ex: Exception) {
      Timber.e(ex, "Could not update widget configuration")
      UpdateResult.Error(SingleCall.Result.UnknownError)
    }
  }

  private fun loadValue(configuration: WidgetConfiguration, withUnit: Boolean) =
    when (configuration.subjectFunction) {
      SuplaFunction.THERMOMETER,
      SuplaFunction.HUMIDITY,
      SuplaFunction.HUMIDITY_AND_TEMPERATURE ->
        handleThermometerWidget(configuration, withUnit)

      SuplaFunction.GENERAL_PURPOSE_METER,
      SuplaFunction.GENERAL_PURPOSE_MEASUREMENT ->
        handleGpmWidget(configuration, withUnit)

      SuplaFunction.CONTAINER,
      SuplaFunction.WATER_TANK,
      SuplaFunction.SEPTIC_TANK ->
        handleContainer(configuration)

      else -> {
        Timber.w("Trying to update widget value of unsupported function ${configuration.subjectFunction}")
        null
      }
    }

  private fun handleThermometerWidget(configuration: WidgetConfiguration, withUnit: Boolean): WidgetConfiguration {
    val temperatureValue = loadValue(configuration) as? TemperatureAndHumidity
    return configuration.copy(value = temperatureValue.format(configuration.subjectFunction, withUnit))
  }

  private fun handleGpmWidget(configuration: WidgetConfiguration, withUnit: Boolean): WidgetConfiguration {
    val channelConfig = try {
      loadChannelConfigUseCase(configuration.itemId).blockingGet()
    } catch (_: Exception) {
      null
    }

    val doubleValue = (loadValue(configuration) as? DoubleValue)?.value ?: GpmValueProvider.UNKNOWN_VALUE

    val stringValue = gpmValueFormatter.format(
      value = doubleValue,
      format = (channelConfig as? SuplaChannelGeneralPurposeBaseConfig).toValueFormat(withUnit)
    )

    return configuration.copy(value = stringValue)
  }

  private fun handleContainer(configuration: WidgetConfiguration): WidgetConfiguration {
    val level = loadValue(configuration) as? ContainerLevel
    val formatted = ContainerValueFormatter.format(level)
    return configuration.copy(value = formatted)
  }

  private fun loadValue(configuration: WidgetConfiguration): ChannelValue =
    singleCallProvider.provide(configuration.profileId)
      .getChannelValue(configuration.itemId)

  private fun TemperatureAndHumidity?.format(function: SuplaFunction, withUnit: Boolean): String {
    if (this == null) {
      return NO_VALUE_TEXT
    }

    return when (function) {
      SuplaFunction.THERMOMETER -> thermometerValueFormatter.format(temperature, ValueFormat(withUnit))
      SuplaFunction.HUMIDITY -> HumidityValueFormatter.format(humidity, ValueFormat(withUnit))
      SuplaFunction.HUMIDITY_AND_TEMPERATURE -> thermometerAndHumidityValueFormatter.format(this, ValueFormat(withUnit))
      else -> NO_VALUE_TEXT
    }
  }
}
