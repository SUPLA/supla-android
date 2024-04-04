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

import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.db.ChannelGroup
import org.supla.android.db.ChannelValue
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_ALARMARMAMENTSENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
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
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChannelStateUseCase @Inject constructor() {

  operator fun invoke(function: Int, value: ValueStateWrapper): ChannelState {
    if (value.online.not()) {
      return getOfflineState(function, value)
    }

    return when (function) {
      SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK,
      SUPLA_CHANNELFNC_CONTROLLINGTHEGATE,
      SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR,
      SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK -> getOpenClose(value.subValueHi)

      SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
      SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW,
      SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND ->
        if (value.rollerShutterClosed) ChannelState(ChannelState.Value.CLOSED) else ChannelState(ChannelState.Value.OPEN)

      SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY,
      SUPLA_CHANNELFNC_OPENSENSOR_GATE,
      SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR,
      SUPLA_CHANNELFNC_OPENSENSOR_DOOR,
      SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER,
      SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW,
      SUPLA_CHANNELFNC_OPENSENSOR_ROOFWINDOW,
      SUPLA_CHANNELFNC_VALVE_OPENCLOSE,
      SUPLA_CHANNELFNC_VALVE_PERCENTAGE -> getOpenClose(value.isClosed)

      SUPLA_CHANNELFNC_POWERSWITCH,
      SUPLA_CHANNELFNC_STAIRCASETIMER,
      SUPLA_CHANNELFNC_NOLIQUIDSENSOR,
      SUPLA_CHANNELFNC_MAILSENSOR,
      SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS,
      SUPLA_CHANNELFNC_HOTELCARDSENSOR,
      SUPLA_CHANNELFNC_ALARMARMAMENTSENSOR,
      SUPLA_CHANNELFNC_LIGHTSWITCH -> getOnOff(value.isClosed)

      SUPLA_CHANNELFNC_DIMMER -> getOnOff(value.brightness > 0)
      SUPLA_CHANNELFNC_RGBLIGHTING -> getOnOff(value.colorBrightness > 0)

      SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING -> {
        val first = if (value.brightness > 0) ChannelState.Value.ON else ChannelState.Value.OFF
        val second = if (value.colorBrightness > 0) ChannelState.Value.ON else ChannelState.Value.OFF

        ChannelState(ChannelState.Value.COMPLEX, listOf(first, second))
      }

      SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL,
      SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL ->
        if (value.transparent) {
          ChannelState(ChannelState.Value.TRANSPARENT)
        } else {
          ChannelState(ChannelState.Value.OPAQUE)
        }

      SUPLA_CHANNELFNC_HVAC_THERMOSTAT -> {
        when (value.thermostatSubfunction) {
          ThermostatSubfunction.HEAT -> ChannelState(ChannelState.Value.HEAT)
          else -> ChannelState(ChannelState.Value.COOL)
        }
      }

      else -> ChannelState(ChannelState.Value.NOT_USED)
    }
  }

  private fun getOfflineState(function: Int, value: ValueStateWrapper): ChannelState {
    return when (function) {
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
      SUPLA_CHANNELFNC_VALVE_PERCENTAGE -> ChannelState(ChannelState.Value.OPEN)

      SUPLA_CHANNELFNC_POWERSWITCH,
      SUPLA_CHANNELFNC_STAIRCASETIMER,
      SUPLA_CHANNELFNC_NOLIQUIDSENSOR,
      SUPLA_CHANNELFNC_MAILSENSOR,
      SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS,
      SUPLA_CHANNELFNC_HOTELCARDSENSOR,
      SUPLA_CHANNELFNC_ALARMARMAMENTSENSOR,
      SUPLA_CHANNELFNC_LIGHTSWITCH,
      SUPLA_CHANNELFNC_DIMMER,
      SUPLA_CHANNELFNC_RGBLIGHTING -> ChannelState(ChannelState.Value.OFF)

      SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING ->
        ChannelState(ChannelState.Value.COMPLEX, listOf(ChannelState.Value.OFF, ChannelState.Value.OFF))

      SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL,
      SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL ->
        ChannelState(ChannelState.Value.OPAQUE)

      SUPLA_CHANNELFNC_HVAC_THERMOSTAT ->
        when (value.thermostatSubfunction) {
          ThermostatSubfunction.HEAT -> ChannelState(ChannelState.Value.HEAT)
          else -> ChannelState(ChannelState.Value.COOL)
        }

      else -> ChannelState(ChannelState.Value.NOT_USED)
    }
  }

  private fun getOpenClose(value: Int) =
    if ((value and 0x2) == 0x2 && (value and 0x1) == 0) {
      ChannelState(ChannelState.Value.PARTIALLY_OPENED)
    } else if (value > 0) {
      ChannelState(ChannelState.Value.CLOSED)
    } else {
      ChannelState(ChannelState.Value.OPEN)
    }

  private fun getOpenClose(isClosed: Boolean) =
    if (isClosed) {
      ChannelState(ChannelState.Value.CLOSED)
    } else {
      ChannelState(ChannelState.Value.OPEN)
    }

  private fun getOnOff(isOn: Boolean) =
    if (isOn) {
      ChannelState(ChannelState.Value.ON)
    } else {
      ChannelState(ChannelState.Value.OFF)
    }
}

interface ValueStateWrapper {

  val online: Boolean
  val subValueHi: Int
  val isClosed: Boolean
  val brightness: Int
  val colorBrightness: Int
  val transparent: Boolean
  val thermostatSubfunction: ThermostatSubfunction?
  val rollerShutterClosed: Boolean
}

class ChannelValueEntityStateWrapper(private val channelValueEntity: ChannelValueEntity) : ValueStateWrapper {
  override val online: Boolean
    get() = channelValueEntity.online
  override val subValueHi: Int
    get() = channelValueEntity.getSubValueHi().toInt()
  override val isClosed: Boolean
    get() = channelValueEntity.isClosed()
  override val brightness: Int
    get() = channelValueEntity.asBrightness().toInt()
  override val colorBrightness: Int
    get() = channelValueEntity.asBrightnessColor().toInt()
  override val transparent: Boolean
    get() = channelValueEntity.asDigiglassValue().isAnySectionTransparent
  override val thermostatSubfunction: ThermostatSubfunction
    get() = channelValueEntity.asThermostatValue().subfunction
  override val rollerShutterClosed: Boolean
    get() {
      val percentage = channelValueEntity.asRollerShutterValue().position
      val subValueHi = channelValueEntity.getSubValueHi()
      return (subValueHi > 0 && percentage < 100) || percentage >= 100
    }
}

class ChannelGroupEntityStateWrapper(private val group: ChannelGroupEntity) : ValueStateWrapper {
  override val online: Boolean
    get() = group.online > 0
  override val subValueHi: Int
    get() = if (group.getActivePercentage() >= 100) 1 else 0
  override val isClosed: Boolean
    get() = group.getActivePercentage() >= 100
  override val brightness: Int
    get() = if (group.getActivePercentage(2) >= 100) 1 else 0
  override val colorBrightness: Int
    get() = if (group.getActivePercentage(1) >= 100) 1 else 0
  override val transparent: Boolean
    get() = false
  override val thermostatSubfunction: ThermostatSubfunction?
    get() = null
  override val rollerShutterClosed: Boolean
    get() = group.getActivePercentage() >= 100
}

class ChannelValueStateWrapper(private val value: ChannelValue?) : ValueStateWrapper {
  override val online: Boolean
    get() = value?.onLine ?: false
  override val subValueHi: Int
    get() = value?.subValueHi?.toInt() ?: 0
  override val isClosed: Boolean
    get() = value?.isClosed ?: false
  override val brightness: Int
    get() = value?.brightness?.toInt() ?: 0
  override val colorBrightness: Int
    get() = value?.colorBrightness?.toInt() ?: 0
  override val transparent: Boolean
    get() = value?.digiglassValue?.isAnySectionTransparent ?: false
  override val thermostatSubfunction: ThermostatSubfunction?
    get() = value?.asThermostatValue()?.subfunction
  override val rollerShutterClosed: Boolean
    get() {
      val percentage = value?.rollerShutterValue?.closingPercentage ?: 0
      val subValueHi = value?.subValueHi ?: 0
      return (subValueHi > 0 && percentage < 100) || percentage >= 100
    }
}

class ChannelGroupStateWrapper(private val group: ChannelGroup) : ValueStateWrapper {
  override val online: Boolean
    get() = group.onLine
  override val subValueHi: Int
    get() = if (group.activePercent >= 100) 1 else 0
  override val isClosed: Boolean
    get() = group.activePercent >= 100
  override val brightness: Int
    get() = if (group.getActivePercent(2) >= 100) 1 else 0
  override val colorBrightness: Int
    get() = if (group.getActivePercent(1) >= 100) 1 else 0
  override val transparent: Boolean
    get() = false
  override val thermostatSubfunction: ThermostatSubfunction?
    get() = null
  override val rollerShutterClosed: Boolean
    get() = group.activePercent >= 100
}

val ChannelBase.stateWrapper: ValueStateWrapper?
  get() {
    (this as? Channel)?.let {
      return ChannelValueStateWrapper(it.value)
    }
    (this as? ChannelGroup)?.let {
      return ChannelGroupStateWrapper(it)
    }

    return null
  }
