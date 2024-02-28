package org.supla.android.usecases.channel.valueformatter

import org.supla.android.Preferences
import org.supla.android.data.ValuesFormatter
import org.supla.android.extensions.guardLet
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.singlecall.TemperatureAndHumidity
import org.supla.android.usecases.channel.valueprovider.HumidityAndTemperatureValueProvider
import org.supla.android.usecases.channel.valueprovider.ThermometerValueProvider

class ThermometerAndHumidityValueFormatter(private val preferences: Preferences) : ChannelValueFormatter {

  private val thermometerValueFormatter = ThermometerValueFormatter(preferences)
  private val humidityValueFormatter = HumidityValueFormatter()

  override fun handle(function: Int) =
    function == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE

  override fun format(value: Any, withUnit: Boolean, precision: Int): String {
    val (temperatureAndHumidity) = guardLet(value as? TemperatureAndHumidity) { return ValuesFormatter.NO_VALUE_TEXT }

    val temperatureString = thermometerValueFormatter.format(
      value = temperatureAndHumidity.temperature ?: ThermometerValueProvider.UNKNOWN_VALUE,
      withUnit = withUnit,
      precision = precision
    )
    val humidityString =
      humidityValueFormatter.format(
        value = temperatureAndHumidity.humidity ?: HumidityAndTemperatureValueProvider.UNKNOWN_HUMIDITY_VALUE,
        withUnit = withUnit,
        precision = precision
      )

    return String.format("%s\n%s", temperatureString, humidityString)
  }
}
