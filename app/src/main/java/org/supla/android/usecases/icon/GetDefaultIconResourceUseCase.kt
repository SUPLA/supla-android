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
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITY
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_PRESSURESENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_RAINSENSOR
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
import org.supla.android.usecases.icon.producers.GeneralPurposeMeasurementIconResourceProducer
import org.supla.android.usecases.icon.producers.GeneralPurposeMeterIconResourceProducer
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
import org.supla.android.usecases.icon.producers.ThermometerIconResourceProducer
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
    ThermometerIconResourceProducer(),
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
    static(SUPLA_CHANNELFNC_DEPTHSENSOR, R.drawable.fnc_depth, R.drawable.fnc_depth_nm),
    static(SUPLA_CHANNELFNC_DISTANCESENSOR, R.drawable.fnc_distance, R.drawable.fnc_distance_nm),
    WindowIconResourceProducer(),
    HotelCardIconResourceProducer(),
    AlarmArmamentSensorIconResourceProducer(),
    MailSensorIconResourceProducer(),
    ElectricityMeterIconResourceProducer(),
    static(SUPLA_CHANNELFNC_IC_GAS_METER, R.drawable.fnc_gasmeter, R.drawable.fnc_gasmeter_nm),
    static(SUPLA_CHANNELFNC_IC_WATER_METER, R.drawable.fnc_watermeter, R.drawable.fnc_watermeter_nm),
    static(SUPLA_CHANNELFNC_IC_HEAT_METER, R.drawable.fnc_heatmeter, R.drawable.fnc_heatmeter_nm),
    ThermostatHomePlusIconResourceProducer(),
    ValveIconResourceProducer(),
    DigiglassIconResourceProducer(),
    GeneralPurposeMeasurementIconResourceProducer(),
    GeneralPurposeMeterIconResourceProducer()
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
