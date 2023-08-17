package org.supla.android.usecases.details
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

import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.features.standarddetail.DetailPage
import org.supla.android.lib.SuplaChannelValue
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNEL_FLAG_COUNTDOWN_TIMER_SUPPORTED
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProvideDetailTypeUseCase @Inject constructor() {

  operator fun invoke(channelBase: ChannelBase): DetailType? = when (channelBase.func) {
    SuplaConst.SUPLA_CHANNELFNC_DIMMER,
    SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING,
    SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING ->
      LegacyDetailType.RGBW
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW ->
      LegacyDetailType.RS
    SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH,
    SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
    SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER -> {
      SwitchDetailType(getSwitchDetailPages(channelBase))
    }
    SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER ->
      LegacyDetailType.EM
    SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER ->
      LegacyDetailType.IC
    SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ->
      LegacyDetailType.TEMPERATURE
    SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ->
      LegacyDetailType.TEMPERATURE_HUMIDITY
    SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS ->
      LegacyDetailType.THERMOSTAT_HP
    SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT,
    SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_COOL,
    SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO,
    SuplaConst.SUPLA_CHANNELFNC_HVAC_DRYER,
    SuplaConst.SUPLA_CHANNELFNC_HVAC_FAN,
    SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_DIFFERENTIAL,
    SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER ->
      ThermostatDetailType(listOf(DetailPage.THERMOSTAT, DetailPage.SCHEDULE))
    SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL,
    SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL ->
      LegacyDetailType.DIGIGLASS
    else -> null
  }

  private fun getSwitchDetailPages(channelBase: ChannelBase): List<DetailPage> {
    return if (channelBase is Channel) {
      val list = mutableListOf(DetailPage.SWITCH)
      if (channelBase.flags.and(SUPLA_CHANNEL_FLAG_COUNTDOWN_TIMER_SUPPORTED) > 0 && channelBase.func != SUPLA_CHANNELFNC_STAIRCASETIMER) {
        list.add(DetailPage.TIMER)
      }
      if (channelBase.value?.subValueType == SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS.toShort()) {
        list.add(DetailPage.HISTORY_IC)
      } else if (channelBase.value?.subValueType == SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort()) {
        list.add(DetailPage.HISTORY_EM)
      }
      list
    } else {
      listOf(DetailPage.SWITCH)
    }
  }
}

sealed interface DetailType : Serializable

enum class LegacyDetailType : DetailType {
  RGBW,
  RS,
  IC,
  EM,
  TEMPERATURE,
  TEMPERATURE_HUMIDITY,
  THERMOSTAT_HP,
  DIGIGLASS
}

data class SwitchDetailType(
  val pages: List<DetailPage>
) : DetailType

data class ThermostatDetailType(
  val pages: List<DetailPage>
) : DetailType
