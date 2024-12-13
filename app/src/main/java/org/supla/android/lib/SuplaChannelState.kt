package org.supla.android.lib
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

import org.supla.android.R
import org.supla.android.data.source.local.entity.ChannelStateEntity
import org.supla.android.extensions.ipV4String
import org.supla.android.tools.UsedFromNativeCode
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.extensions.toHex
import org.supla.core.shared.infrastructure.LocalizedString
import java.io.Serializable
import java.util.Locale

@UsedFromNativeCode
class SuplaChannelState(
  val channelId: Int,
  private val fields: Int,
  private val rawDefaultIconField: Int,
  private val rawIpv4: Int,
  private val rawMacAddress: ByteArray,
  private val rawBatteryLevel: Byte,
  private val rawBatteryPowered: Byte,
  private val rawWifiRssi: Byte,
  private val rawWifiSignalStrength: Byte,
  private val rawBridgeNodeOnline: Byte,
  private val rawBridgeNodeSignalStrength: Byte,
  private val rawUptime: Int,
  private val rawConnectionUptime: Int,
  private val rawBatteryHealth: Byte,
  private val rawLastConnectionResetCause: Byte,
  private val rawLightSourceLifespan: Int,
  private val rawLightSourceLifespanLeft: Int
) : Serializable {

  val ipV4: String?
    get() = hasField(FIELD_IPV4).ifTrue { rawIpv4.ipV4String }

  val macAddress: String?
    get() = hasField(FIELD_MAC).ifTrue { rawMacAddress.toHex(":") }

  val batteryLevel: Int?
    get() = hasField(FIELD_BATTERYLEVEL).ifTrue { rawBatteryLevel.toInt() }

  val batteryLevelString: String?
    get() = batteryLevel?.let { "$it%" }

  val batteryPowered: Boolean?
    get() = hasField(FIELD_BATTERYPOWERED).ifTrue { rawBatteryPowered > 0 }

  val batterPoweredString: LocalizedString?
    get() = batteryPowered?.let { LocalizedString.WithResource(if (it) R.string.yes else R.string.no) }

  val wifiRssi: Int?
    get() = hasField(FIELD_WIFIRSSI).ifTrue { rawWifiRssi.toInt() }

  val wifiRssiString: String?
    get() = wifiRssi?.let { "$it" }

  val wifiSignalStrength: Int?
    get() = hasField(FIELD_WIFISIGNALSTRENGTH).ifTrue { rawWifiSignalStrength.toInt() }

  val wifiSignalStrengthString: String?
    get() = wifiSignalStrength?.let { "$it%" }

  val bridgeNodeOnline: Boolean?
    get() = hasField(FIELD_BRIDGENODEONLINE).ifTrue { rawBridgeNodeOnline > 0 }

  val bridgeNodeSignalStrength: Int?
    get() = hasField(FIELD_BRIDGENODESIGNALSTRENGTH).ifTrue { rawBridgeNodeSignalStrength.toInt() }

  val bridgeNodeSignalStrengthString: String?
    get() = bridgeNodeSignalStrength?.let { "$it%" }

  val uptime: Int?
    get() = hasField(FIELD_UPTIME).ifTrue { rawUptime }

  val uptimeString: LocalizedString?
    get() = uptime?.let { getUptimeString(it) }

  val connectionUptime: Int?
    get() = hasField(FIELD_CONNECTIONUPTIME).ifTrue { rawConnectionUptime }

  val connectionUptimeString: LocalizedString?
    get() = connectionUptime?.let { getUptimeString(it) }

  val batteryHealth: Int?
    get() = hasField(FIELD_BATTERYHEALTH).ifTrue { rawBatteryHealth.toInt() }

  val batteryHealthString: String?
    get() = batteryHealth?.let { "$it%" }

  val lastConnectionResetCause: Int?
    get() = hasField(FIELD_LASTCONNECTIONRESETCAUSE).ifTrue { rawLastConnectionResetCause.toInt() }

  val lastConnectionResetCauseString: LocalizedString?
    get() = lastConnectionResetCause?.let { cause ->
      val causeResources = listOf(
        R.string.lastconnectionresetcause_unknown,
        R.string.lastconnectionresetcause_activity_timeout,
        R.string.lastconnectionresetcause_wifi_connection_lost,
        R.string.lastconnectionresetcause_server_connection_lost
      )
      causeResources.getOrNull(cause)?.let { LocalizedString.WithResource(it) } ?: LocalizedString.Constant("$cause")
    }

  val lightSourceLifespan: Int?
    get() = hasField(FIELD_LIGHTSOURCELIFESPAN).ifTrue { rawLightSourceLifespan }

  val lightSourceLifespanString: String?
    get() = lightSourceLifespan?.let { lightSourceLifespan ->
      val left = lightSourceLifespanLeft ?: lightSourceOperatingTimePercentLeft

      if (left != null) {
        String.format(Locale.getDefault(), "%dh (%.2f%%)", lightSourceLifespan, left)
      } else {
        String.format(Locale.getDefault(), "%dh", lightSourceLifespan)
      }
    }

  val lightSourceLifespanLeft: Float?
    get() = if (hasField(FIELD_LIGHTSOURCELIFESPAN) && !hasField(FIELD_LIGHTSOURCELIFEOPERATINGTIME)) {
      rawLightSourceLifespanLeft / 100f
    } else {
      null
    }

  val lightSourceOperatingTime: Int?
    get() = hasField(FIELD_LIGHTSOURCELIFEOPERATINGTIME).ifTrue { rawLightSourceLifespanLeft }

  val lightSourceOperatingTimeString: String?
    get() = lightSourceOperatingTime?.let {
      String.format(Locale.getDefault(), "%02dh %02d:%02d", it / 3600, it % 3600 / 60, it % 60)
    }

  val lightSourceOperatingTimePercent: Float? =
    lightSourceOperatingTime?.let { lightSourceOperatingTime ->
      lightSourceLifespan?.let { lightSourceLifespan ->
        (lightSourceLifespan > 0).ifTrue { lightSourceOperatingTime / 36f / lightSourceLifespan }
      }
    }

  val lightSourceOperatingTimePercentLeft: Float? =
    lightSourceOperatingTimePercent?.let { 100 - it }

  fun toEntity(profileId: Long): ChannelStateEntity {
    return ChannelStateEntity(
      batteryHealth = batteryHealth,
      batteryLevel = batteryLevel,
      batteryPowered = batteryPowered,
      bridgeNodeOnline = bridgeNodeOnline,
      bridgeNodeSignalStrength = bridgeNodeSignalStrength,
      connectionUptime = connectionUptime,
      ipV4 = ipV4,
      lastConnectionResetCause = lastConnectionResetCause,
      lightSourceLifespan = lightSourceLifespan,
      lightSourceLifespanLeft = lightSourceLifespanLeft,
      lightSourceOperatingTime = lightSourceOperatingTime,
      macAddress = macAddress,
      uptime = uptime,
      wifiRssi = wifiRssi,
      wifiSignalStrength = wifiSignalStrength,
      channelId = channelId,
      profileId = profileId
    )
  }

  private fun hasField(field: Int): Boolean = (fields and field) > 0

  private fun getUptimeString(time: Int): LocalizedString = LocalizedString.WithResourceIntIntIntInt(
    id = R.string.channel_state_uptime,
    arg1 = time / 86400,
    arg2 = time % 86400 / 3600,
    arg3 = time % 3600 / 60,
    arg4 = time % 60
  )

  companion object {
    const val FIELD_IPV4: Int = 0x0001
    const val FIELD_MAC: Int = 0x0002
    const val FIELD_BATTERYLEVEL: Int = 0x0004
    const val FIELD_BATTERYPOWERED: Int = 0x0008
    const val FIELD_WIFIRSSI: Int = 0x0010
    const val FIELD_WIFISIGNALSTRENGTH: Int = 0x0020
    const val FIELD_BRIDGENODESIGNALSTRENGTH: Int = 0x0040
    const val FIELD_UPTIME: Int = 0x0080
    const val FIELD_CONNECTIONUPTIME: Int = 0x0100
    const val FIELD_BATTERYHEALTH: Int = 0x0200
    const val FIELD_BRIDGENODEONLINE: Int = 0x0400
    const val FIELD_LASTCONNECTIONRESETCAUSE: Int = 0x0800
    const val FIELD_LIGHTSOURCELIFESPAN: Int = 0x1000
    const val FIELD_LIGHTSOURCELIFEOPERATINGTIME: Int = 0x2000
  }
}
