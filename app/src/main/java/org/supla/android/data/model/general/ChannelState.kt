package org.supla.android.data.model.general

import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_ALARMARMAMENTSENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HOTELCARDSENSOR
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
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_VALVE_PERCENTAGE

/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the``````
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

data class ChannelState(
  val value: Value,
  val complex: List<Value>? = null
) {

  enum class Value {
    // active states
    OPEN,
    ON,
    TRANSPARENT,

    // inactive states
    PARTIALLY_OPENED,
    CLOSED,
    OFF,
    OPAQUE,

    // thermostat
    HEAT,
    COOL,

    // fulfillment
    FULL,
    HALF,
    EMPTY,

    // others
    NOT_USED,
    COMPLEX
  }

  fun isActive(): Boolean {
    return when (value) {
      Value.CLOSED, Value.ON, Value.TRANSPARENT -> true
      else -> false
    }
  }

  companion object {
    fun active(function: Int): ChannelState =
      when (function) {
        SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL,
        SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL -> ChannelState(Value.TRANSPARENT)

        SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK,
        SUPLA_CHANNELFNC_CONTROLLINGTHEGATE,
        SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR,
        SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK,
        SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
        SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW,
        SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY,
        SUPLA_CHANNELFNC_OPENSENSOR_GATE,
        SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR,
        SUPLA_CHANNELFNC_OPENSENSOR_DOOR,
        SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER,
        SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW,
        SUPLA_CHANNELFNC_OPENSENSOR_ROOFWINDOW,
        SUPLA_CHANNELFNC_VALVE_OPENCLOSE,
        SUPLA_CHANNELFNC_VALVE_PERCENTAGE -> ChannelState(Value.CLOSED)

        SUPLA_CHANNELFNC_POWERSWITCH,
        SUPLA_CHANNELFNC_STAIRCASETIMER,
        SUPLA_CHANNELFNC_NOLIQUIDSENSOR,
        SUPLA_CHANNELFNC_MAILSENSOR,
        SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS,
        SUPLA_CHANNELFNC_HOTELCARDSENSOR,
        SUPLA_CHANNELFNC_ALARMARMAMENTSENSOR,
        SUPLA_CHANNELFNC_LIGHTSWITCH,
        SUPLA_CHANNELFNC_DIMMER,
        SUPLA_CHANNELFNC_RGBLIGHTING -> ChannelState(Value.ON)

        SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING -> ChannelState(Value.COMPLEX, listOf(Value.ON, Value.ON))

        else -> ChannelState(Value.NOT_USED)
      }

    fun inactive(function: Int): ChannelState =
      when (function) {
        SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL,
        SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL -> ChannelState(Value.OPAQUE)

        SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK,
        SUPLA_CHANNELFNC_CONTROLLINGTHEGATE,
        SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR,
        SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK,
        SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
        SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW,
        SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY,
        SUPLA_CHANNELFNC_OPENSENSOR_GATE,
        SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR,
        SUPLA_CHANNELFNC_OPENSENSOR_DOOR,
        SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER,
        SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW,
        SUPLA_CHANNELFNC_OPENSENSOR_ROOFWINDOW,
        SUPLA_CHANNELFNC_VALVE_OPENCLOSE,
        SUPLA_CHANNELFNC_VALVE_PERCENTAGE -> ChannelState(Value.OPEN)

        SUPLA_CHANNELFNC_POWERSWITCH,
        SUPLA_CHANNELFNC_STAIRCASETIMER,
        SUPLA_CHANNELFNC_NOLIQUIDSENSOR,
        SUPLA_CHANNELFNC_MAILSENSOR,
        SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS,
        SUPLA_CHANNELFNC_HOTELCARDSENSOR,
        SUPLA_CHANNELFNC_ALARMARMAMENTSENSOR,
        SUPLA_CHANNELFNC_LIGHTSWITCH,
        SUPLA_CHANNELFNC_DIMMER,
        SUPLA_CHANNELFNC_RGBLIGHTING -> ChannelState(Value.OFF)

        SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING -> ChannelState(Value.COMPLEX, listOf(Value.OFF, Value.OFF))

        else -> ChannelState(Value.NOT_USED)
      }
  }
}
