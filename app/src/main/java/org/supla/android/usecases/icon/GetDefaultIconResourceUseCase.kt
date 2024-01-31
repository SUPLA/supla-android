package org.supla.android.usecases.icon
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

import androidx.annotation.DrawableRes
import org.supla.android.R
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.model.general.IconType
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITY
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_PRESSURESENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_RAINSENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_WEIGHTSENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_WINDSENSOR
import org.supla.android.usecases.icon.producers.AlarmArmamentSensorIconResourceProducer
import org.supla.android.usecases.icon.producers.DigiglassIconResourceProducer
import org.supla.android.usecases.icon.producers.DimmerAndRgbIconResourceProducer
import org.supla.android.usecases.icon.producers.DimmerIconResourceProducer
import org.supla.android.usecases.icon.producers.DoorIconResourceProducer
import org.supla.android.usecases.icon.producers.ElectricityMeterIconResourceProducer
import org.supla.android.usecases.icon.producers.GarageDoorIconResourceProducer
import org.supla.android.usecases.icon.producers.GateIconResourceProducer
import org.supla.android.usecases.icon.producers.GatewayIconResourceProducer
import org.supla.android.usecases.icon.producers.HotelCardIconResourceProducer
import org.supla.android.usecases.icon.producers.HumidityAndTemperatureIconResourceProducer
import org.supla.android.usecases.icon.producers.LightSwitchIconResourceProducer
import org.supla.android.usecases.icon.producers.LiquidSensorIconResourceProducer
import org.supla.android.usecases.icon.producers.MailSensorIconResourceProducer
import org.supla.android.usecases.icon.producers.PowerSwitchIconResourceProducer
import org.supla.android.usecases.icon.producers.RgbLightingIconResourceProducer
import org.supla.android.usecases.icon.producers.RollerShutterIconResourceProducer
import org.supla.android.usecases.icon.producers.RoofWindowIconResourceProducer
import org.supla.android.usecases.icon.producers.StaircaseTimerIconResourceProducer
import org.supla.android.usecases.icon.producers.StaticIconResourceProducer
import org.supla.android.usecases.icon.producers.ThermostatHomePlusIconResourceProducer
import org.supla.android.usecases.icon.producers.ThermostatHvacIconResourceProducer
import org.supla.android.usecases.icon.producers.ValveIconResourceProducer
import org.supla.android.usecases.icon.producers.WindowIconResourceProducer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetDefaultIconResourceUseCase @Inject constructor() {

  private val producers = listOf(
    GatewayIconResourceProducer(),
    GateIconResourceProducer(),
    GarageDoorIconResourceProducer(),
    DoorIconResourceProducer(),
    RollerShutterIconResourceProducer(),
    RoofWindowIconResourceProducer(),
    PowerSwitchIconResourceProducer(),
    LightSwitchIconResourceProducer(),
    StaircaseTimerIconResourceProducer(),
    static(SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER, R.drawable.fnc_thermostat_dhw, R.drawable.fnc_thermostat_dhw_nm),
    ThermostatHvacIconResourceProducer(),
    static(SUPLA_CHANNELFNC_THERMOMETER, R.drawable.thermometer, R.drawable.thermometer_nightmode),
    static(SUPLA_CHANNELFNC_HUMIDITY, R.drawable.humidity, R.drawable.humidity_nightmode),
    HumidityAndTemperatureIconResourceProducer(),
    static(SUPLA_CHANNELFNC_WINDSENSOR, R.drawable.wind, R.drawable.wind_nightmode),
    static(SUPLA_CHANNELFNC_PRESSURESENSOR, R.drawable.pressure, R.drawable.pressure_nightmode),
    static(SUPLA_CHANNELFNC_RAINSENSOR, R.drawable.rain, R.drawable.rain_nightmode),
    static(SUPLA_CHANNELFNC_WEIGHTSENSOR, R.drawable.weight, R.drawable.weight_nightmode),
    LiquidSensorIconResourceProducer(),
    DimmerIconResourceProducer(),
    RgbLightingIconResourceProducer(),
    DimmerAndRgbIconResourceProducer(),
    static(SUPLA_CHANNELFNC_DEPTHSENSOR, R.drawable.depthsensor, R.drawable.depthsensor_nightmode),
    static(SUPLA_CHANNELFNC_DISTANCESENSOR, R.drawable.distancesensor, R.drawable.distancesensor_nightmode),
    WindowIconResourceProducer(),
    HotelCardIconResourceProducer(),
    AlarmArmamentSensorIconResourceProducer(),
    MailSensorIconResourceProducer(),
    ElectricityMeterIconResourceProducer(),
    static(SUPLA_CHANNELFNC_IC_GAS_METER, R.drawable.gasmeter, R.drawable.gasmeter_nightmode),
    static(SUPLA_CHANNELFNC_IC_WATER_METER, R.drawable.watermeter, R.drawable.watermeter_nightmode),
    static(SUPLA_CHANNELFNC_IC_HEAT_METER, R.drawable.heatmeter, R.drawable.heatmeter_nightmode),
    ThermostatHomePlusIconResourceProducer(),
    ValveIconResourceProducer(),
    DigiglassIconResourceProducer(),
    static(SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT, R.drawable.fnc_general_purpose_channel, R.drawable.fnc_general_purpose_channel_nm),
    static(SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER, R.drawable.fnc_general_purpose_channel, R.drawable.fnc_general_purpose_channel_nm)
  )

  @DrawableRes
  operator fun invoke(data: IconData): Int {
    producers.forEach { producer ->
      if (producer.accepts(data.function)) {
        producer.produce(data)?.let {
          return it
        }
      }
    }

    return R.drawable.ic_unknown_channel
  }
}

interface IconResourceProducer {
  fun accepts(function: Int): Boolean

  @DrawableRes
  fun produce(data: IconData): Int?
}

data class IconData(
  val function: Int,
  val altIcon: Int,
  val state: ChannelState = ChannelState(ChannelState.Value.NOT_USED),
  val type: IconType = IconType.SINGLE,
  val nightMode: Boolean = false
) {

  fun icon(@DrawableRes day: Int, @DrawableRes night: Int): Int =
    if (nightMode) night else day
}

private fun static(function: Int, @DrawableRes day: Int, @DrawableRes night: Int) =
  StaticIconResourceProducer(function, day, night)
