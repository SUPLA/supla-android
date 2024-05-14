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
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_ALARMARMAMENTSENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HOTELCARDSENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITY
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_MAILSENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_NOLIQUIDSENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_DOOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROOFWINDOW
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_PRESSURESENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_RAINSENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_TERRACE_AWNING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_WEIGHTSENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_WINDSENSOR
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChannelDefaultCaptionUseCase @Inject constructor() {

  operator fun invoke(function: Int) =
    when (function) {
      SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY -> string(R.string.channel_caption_gatewayopeningsensor)
      SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK -> string(R.string.channel_caption_gateway)
      SUPLA_CHANNELFNC_OPENSENSOR_GATE -> string(R.string.channel_caption_gateopeningsensor)
      SUPLA_CHANNELFNC_CONTROLLINGTHEGATE -> string(R.string.channel_caption_gate)
      SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR -> string(R.string.channel_caption_garagedooropeningsensor)
      SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR -> string(R.string.channel_caption_garagedoor)
      SUPLA_CHANNELFNC_OPENSENSOR_DOOR -> string(R.string.channel_caption_dooropeningsensor)
      SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK -> string(R.string.channel_caption_door)
      SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER -> string(R.string.channel_caption_rsopeningsensor)
      SUPLA_CHANNELFNC_OPENSENSOR_ROOFWINDOW -> string(R.string.channel_caption_roofwindowopeningsensor)
      SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER -> string(R.string.channel_caption_rollershutter)
      SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW -> string(R.string.channel_caption_roofwindow)
      SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND -> string(R.string.channel_caption_facade_blinds)
      SUPLA_CHANNELFNC_POWERSWITCH -> string(R.string.channel_caption_powerswith)
      SUPLA_CHANNELFNC_LIGHTSWITCH -> string(R.string.channel_caption_lightswith)
      SUPLA_CHANNELFNC_THERMOMETER -> string(R.string.channel_caption_thermometer)
      SUPLA_CHANNELFNC_HUMIDITY -> string(R.string.channel_caption_humidity)
      SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE -> string(R.string.channel_caption_humidityandtemperature)
      SUPLA_CHANNELFNC_WINDSENSOR -> string(R.string.channel_caption_windsensor)
      SUPLA_CHANNELFNC_PRESSURESENSOR -> string(R.string.channel_caption_pressuresensor)
      SUPLA_CHANNELFNC_RAINSENSOR -> string(R.string.channel_caption_rainsensor)
      SUPLA_CHANNELFNC_WEIGHTSENSOR -> string(R.string.channel_caption_weightsensor)
      SUPLA_CHANNELFNC_NOLIQUIDSENSOR -> string(R.string.channel_caption_noliquidsensor)
      SUPLA_CHANNELFNC_DIMMER -> string(R.string.channel_caption_dimmer)
      SUPLA_CHANNELFNC_RGBLIGHTING -> string(R.string.channel_caption_rgblighting)
      SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING -> string(R.string.channel_caption_dimmerandrgblighting)
      SUPLA_CHANNELFNC_DEPTHSENSOR -> string(R.string.channel_caption_depthsensor)
      SUPLA_CHANNELFNC_DISTANCESENSOR -> string(R.string.channel_caption_distancesensor)
      SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW -> string(R.string.channel_caption_windowopeningsensor)
      SUPLA_CHANNELFNC_HOTELCARDSENSOR -> string(R.string.channel_caption_hotelcard)
      SUPLA_CHANNELFNC_ALARMARMAMENTSENSOR -> string(R.string.channel_caption_alarm_armament)
      SUPLA_CHANNELFNC_MAILSENSOR -> string(R.string.channel_caption_mailsensor)
      SUPLA_CHANNELFNC_STAIRCASETIMER -> string(R.string.channel_caption_staircasetimer)
      SUPLA_CHANNELFNC_IC_GAS_METER -> string(R.string.channel_caption_gasmeter)
      SUPLA_CHANNELFNC_IC_WATER_METER -> string(R.string.channel_caption_watermeter)
      SUPLA_CHANNELFNC_IC_HEAT_METER -> string(R.string.channel_caption_heatmeter)
      SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS -> string(R.string.channel_caption_thermostat_hp_homeplus)
      SUPLA_CHANNELFNC_VALVE_OPENCLOSE -> string(R.string.channel_caption_valve)
      SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT -> string(R.string.channel_caption_general_purpose_measurement)
      SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER -> string(R.string.channel_caption_general_purpose_meter)
      SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
      SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER -> string(R.string.channel_caption_thermostat)

      SUPLA_CHANNELFNC_ELECTRICITY_METER,
      SUPLA_CHANNELFNC_IC_ELECTRICITY_METER -> string(R.string.channel_caption_electricitymeter)

      SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL,
      SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL -> string(R.string.channel_caption_digiglass)

      SUPLA_CHANNELFNC_TERRACE_AWNING -> string(R.string.channel_caption_terrace_awning)

      else -> string(R.string.channel_not_supported)
    }

  private fun string(@StringRes stringRes: Int): StringProvider = {
    it.getString(stringRes)
  }
}
