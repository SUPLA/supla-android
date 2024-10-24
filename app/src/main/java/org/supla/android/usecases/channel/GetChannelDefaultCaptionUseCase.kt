package org.supla.android.usecases.channel
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

import androidx.annotation.StringRes
import org.supla.android.R
import org.supla.android.core.ui.StringProvider
import org.supla.core.shared.data.SuplaChannelFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChannelDefaultCaptionUseCase @Inject constructor() {

  operator fun invoke(function: SuplaChannelFunction) =
    when (function) {
      SuplaChannelFunction.OPEN_SENSOR_GATEWAY -> string(R.string.channel_caption_gatewayopeningsensor)
      SuplaChannelFunction.CONTROLLING_THE_GATEWAY_LOCK -> string(R.string.channel_caption_gateway)
      SuplaChannelFunction.OPEN_SENSOR_GATE -> string(R.string.channel_caption_gateopeningsensor)
      SuplaChannelFunction.CONTROLLING_THE_GATE -> string(R.string.channel_caption_gate)
      SuplaChannelFunction.OPEN_SENSOR_GARAGE_DOOR -> string(R.string.channel_caption_garagedooropeningsensor)
      SuplaChannelFunction.CONTROLLING_THE_GARAGE_DOOR -> string(R.string.channel_caption_garagedoor)
      SuplaChannelFunction.OPEN_SENSOR_DOOR -> string(R.string.channel_caption_dooropeningsensor)
      SuplaChannelFunction.CONTROLLING_THE_DOOR_LOCK -> string(R.string.channel_caption_door)
      SuplaChannelFunction.OPEN_SENSOR_ROLLER_SHUTTER -> string(R.string.channel_caption_rsopeningsensor)
      SuplaChannelFunction.OPEN_SENSOR_ROOF_WINDOW -> string(R.string.channel_caption_roofwindowopeningsensor)
      SuplaChannelFunction.CONTROLLING_THE_ROLLER_SHUTTER -> string(R.string.channel_caption_rollershutter)
      SuplaChannelFunction.CONTROLLING_THE_ROOF_WINDOW -> string(R.string.channel_caption_roofwindow)
      SuplaChannelFunction.CONTROLLING_THE_FACADE_BLIND -> string(R.string.channel_caption_facade_blinds)
      SuplaChannelFunction.POWER_SWITCH -> string(R.string.channel_caption_powerswith)
      SuplaChannelFunction.LIGHTSWITCH -> string(R.string.channel_caption_lightswith)
      SuplaChannelFunction.THERMOMETER -> string(R.string.channel_caption_thermometer)
      SuplaChannelFunction.HUMIDITY -> string(R.string.channel_caption_humidity)
      SuplaChannelFunction.HUMIDITY_AND_TEMPERATURE -> string(R.string.channel_caption_humidityandtemperature)
      SuplaChannelFunction.WIND_SENSOR -> string(R.string.channel_caption_windsensor)
      SuplaChannelFunction.PRESSURE_SENSOR -> string(R.string.channel_caption_pressuresensor)
      SuplaChannelFunction.RAIN_SENSOR -> string(R.string.channel_caption_rainsensor)
      SuplaChannelFunction.WEIGHT_SENSOR -> string(R.string.channel_caption_weightsensor)
      SuplaChannelFunction.NO_LIQUID_SENSOR -> string(R.string.channel_caption_noliquidsensor)
      SuplaChannelFunction.DIMMER -> string(R.string.channel_caption_dimmer)
      SuplaChannelFunction.RGB_LIGHTING -> string(R.string.channel_caption_rgblighting)
      SuplaChannelFunction.DIMMER_AND_RGB_LIGHTING -> string(R.string.channel_caption_dimmerandrgblighting)
      SuplaChannelFunction.DEPTH_SENSOR -> string(R.string.channel_caption_depthsensor)
      SuplaChannelFunction.DISTANCE_SENSOR -> string(R.string.channel_caption_distancesensor)
      SuplaChannelFunction.OPENING_SENSOR_WINDOW -> string(R.string.channel_caption_windowopeningsensor)
      SuplaChannelFunction.HOTEL_CARD_SENSOR -> string(R.string.channel_caption_hotelcard)
      SuplaChannelFunction.ALARM_ARMAMENT_SENSOR -> string(R.string.channel_caption_alarm_armament)
      SuplaChannelFunction.MAIL_SENSOR -> string(R.string.channel_caption_mailsensor)
      SuplaChannelFunction.STAIRCASE_TIMER -> string(R.string.channel_caption_staircasetimer)
      SuplaChannelFunction.IC_GAS_METER -> string(R.string.channel_caption_gasmeter)
      SuplaChannelFunction.IC_WATER_METER -> string(R.string.channel_caption_watermeter)
      SuplaChannelFunction.IC_HEAT_METER -> string(R.string.channel_caption_heatmeter)
      SuplaChannelFunction.THERMOSTAT_HEATPOL_HOMEPLUS -> string(R.string.channel_caption_thermostat_hp_homeplus)
      SuplaChannelFunction.VALVE_OPEN_CLOSE,
      SuplaChannelFunction.VALVE_PERCENTAGE -> string(R.string.channel_caption_valve)
      SuplaChannelFunction.GENERAL_PURPOSE_MEASUREMENT -> string(R.string.channel_caption_general_purpose_measurement)
      SuplaChannelFunction.GENERAL_PURPOSE_METER -> string(R.string.channel_caption_general_purpose_meter)
      SuplaChannelFunction.HVAC_THERMOSTAT,
      SuplaChannelFunction.HVAC_DOMESTIC_HOT_WATER -> string(R.string.channel_caption_thermostat)

      SuplaChannelFunction.ELECTRICITY_METER,
      SuplaChannelFunction.IC_ELECTRICITY_METER -> string(R.string.channel_caption_electricitymeter)

      SuplaChannelFunction.DIGIGLASS_VERTICAL,
      SuplaChannelFunction.DIGIGLASS_HORIZONTAL -> string(R.string.channel_caption_digiglass)

      SuplaChannelFunction.TERRACE_AWNING -> string(R.string.channel_caption_terrace_awning)
      SuplaChannelFunction.PROJECTOR_SCREEN -> string(R.string.channel_caption_projector_screen)
      SuplaChannelFunction.CURTAIN -> string(R.string.channel_caption_curtain)
      SuplaChannelFunction.VERTICAL_BLIND -> string(R.string.channel_caption_vertical_blinds)
      SuplaChannelFunction.ROLLER_GARAGE_DOOR -> string(R.string.channel_caption_roller_garage_door)
      SuplaChannelFunction.PUMP_SWITCH -> string(R.string.channel_caption_pump_switch)
      SuplaChannelFunction.HEAT_OR_COLD_SOURCE_SWITCH -> string(R.string.channel_caption_heat_or_cold_sourc_switch)

      SuplaChannelFunction.UNKNOWN,
      SuplaChannelFunction.NONE,
      SuplaChannelFunction.RING,
      SuplaChannelFunction.ALARM,
      SuplaChannelFunction.NOTIFICATION,
      SuplaChannelFunction.WEATHER_STATION,
      SuplaChannelFunction.HVAC_THERMOSTAT_HEAT_COOL -> string(R.string.channel_not_supported)
    }

  private fun string(@StringRes stringRes: Int): StringProvider = {
    it.getString(stringRes)
  }
}
