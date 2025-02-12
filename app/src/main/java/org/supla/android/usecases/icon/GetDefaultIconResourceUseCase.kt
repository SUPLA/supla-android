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
import org.supla.android.usecases.icon.producers.AlarmArmamentSensorIconResourceProducer
import org.supla.android.usecases.icon.producers.ContainerIconResourceProducer
import org.supla.android.usecases.icon.producers.CurtainIconResourceProducer
import org.supla.android.usecases.icon.producers.DigiglassIconResourceProducer
import org.supla.android.usecases.icon.producers.DimmerAndRgbIconResourceProducer
import org.supla.android.usecases.icon.producers.DimmerIconResourceProducer
import org.supla.android.usecases.icon.producers.DoorIconResourceProducer
import org.supla.android.usecases.icon.producers.ElectricityMeterIconResourceProducer
import org.supla.android.usecases.icon.producers.FacadeBlindIconResourceProducer
import org.supla.android.usecases.icon.producers.FloodSensorIconResourceProducer
import org.supla.android.usecases.icon.producers.GarageDoorIconResourceProducer
import org.supla.android.usecases.icon.producers.GarageDoorRollerIconResourceProducer
import org.supla.android.usecases.icon.producers.GateIconResourceProducer
import org.supla.android.usecases.icon.producers.GatewayIconResourceProducer
import org.supla.android.usecases.icon.producers.GeneralPurposeMeasurementIconResourceProducer
import org.supla.android.usecases.icon.producers.GeneralPurposeMeterIconResourceProducer
import org.supla.android.usecases.icon.producers.HeatOrColdSourceSwitchIconResourceProducer
import org.supla.android.usecases.icon.producers.HotelCardIconResourceProducer
import org.supla.android.usecases.icon.producers.HumidityAndTemperatureIconResourceProducer
import org.supla.android.usecases.icon.producers.LightSwitchIconResourceProducer
import org.supla.android.usecases.icon.producers.LiquidSensorIconResourceProducer
import org.supla.android.usecases.icon.producers.MailSensorIconResourceProducer
import org.supla.android.usecases.icon.producers.PowerSwitchIconResourceProducer
import org.supla.android.usecases.icon.producers.ProjectorScreenIconResourceProducer
import org.supla.android.usecases.icon.producers.PumpSwitchIconResourceProducer
import org.supla.android.usecases.icon.producers.RgbLightingIconResourceProducer
import org.supla.android.usecases.icon.producers.RollerShutterIconResourceProducer
import org.supla.android.usecases.icon.producers.RoofWindowIconResourceProducer
import org.supla.android.usecases.icon.producers.StaircaseTimerIconResourceProducer
import org.supla.android.usecases.icon.producers.StaticIconResourceProducer
import org.supla.android.usecases.icon.producers.TerraceAwningIconResourceProducer
import org.supla.android.usecases.icon.producers.ThermometerIconResourceProducer
import org.supla.android.usecases.icon.producers.ThermostatHomePlusIconResourceProducer
import org.supla.android.usecases.icon.producers.ThermostatHvacIconResourceProducer
import org.supla.android.usecases.icon.producers.ValveIconResourceProducer
import org.supla.android.usecases.icon.producers.VerticalBlindsIconResourceProducer
import org.supla.android.usecases.icon.producers.WindowIconResourceProducer
import org.supla.core.shared.data.model.general.SuplaFunction
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
    FacadeBlindIconResourceProducer(),
    PowerSwitchIconResourceProducer(),
    LightSwitchIconResourceProducer(),
    StaircaseTimerIconResourceProducer(),
    static(SuplaFunction.HVAC_DOMESTIC_HOT_WATER, R.drawable.fnc_thermostat_dhw),
    ThermostatHvacIconResourceProducer(),
    ThermometerIconResourceProducer(),
    static(SuplaFunction.HUMIDITY, R.drawable.humidity),
    HumidityAndTemperatureIconResourceProducer(),
    static(SuplaFunction.WIND_SENSOR, R.drawable.wind),
    static(SuplaFunction.PRESSURE_SENSOR, R.drawable.pressure),
    static(SuplaFunction.RAIN_SENSOR, R.drawable.rain),
    static(SuplaFunction.WEIGHT_SENSOR, R.drawable.weight),
    LiquidSensorIconResourceProducer(),
    DimmerIconResourceProducer(),
    RgbLightingIconResourceProducer(),
    DimmerAndRgbIconResourceProducer(),
    static(SuplaFunction.DEPTH_SENSOR, R.drawable.fnc_depth),
    static(SuplaFunction.DISTANCE_SENSOR, R.drawable.fnc_distance),
    WindowIconResourceProducer(),
    HotelCardIconResourceProducer(),
    AlarmArmamentSensorIconResourceProducer(),
    MailSensorIconResourceProducer(),
    ElectricityMeterIconResourceProducer(),
    static(SuplaFunction.IC_GAS_METER, R.drawable.fnc_gasmeter),
    static(SuplaFunction.IC_WATER_METER, R.drawable.fnc_watermeter),
    static(SuplaFunction.IC_HEAT_METER, R.drawable.fnc_heatmeter),
    ThermostatHomePlusIconResourceProducer(),
    ValveIconResourceProducer(),
    DigiglassIconResourceProducer(),
    GeneralPurposeMeasurementIconResourceProducer(),
    GeneralPurposeMeterIconResourceProducer(),
    TerraceAwningIconResourceProducer(),
    ProjectorScreenIconResourceProducer(),
    CurtainIconResourceProducer(),
    VerticalBlindsIconResourceProducer,
    GarageDoorRollerIconResourceProducer,
    HeatOrColdSourceSwitchIconResourceProducer,
    PumpSwitchIconResourceProducer,
    ContainerIconResourceProducer,
    FloodSensorIconResourceProducer
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
  fun accepts(function: SuplaFunction): Boolean

  @DrawableRes
  fun produce(data: IconData): Int?
}

data class IconData(
  val function: SuplaFunction,
  val altIcon: Int,
  val state: ChannelState = ChannelState(ChannelState.Value.NOT_USED),
  val type: IconType = IconType.SINGLE
)

private fun static(function: SuplaFunction, @DrawableRes day: Int) =
  StaticIconResourceProducer(function, day)
