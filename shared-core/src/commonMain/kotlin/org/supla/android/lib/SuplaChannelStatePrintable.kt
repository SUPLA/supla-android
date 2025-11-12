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

import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.extensions.localizedString
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.LocalizedStringId
import org.supla.core.shared.infrastructure.localizedString

interface SuplaChannelStatePrintable {

  val channelId: Int
  val ipV4: String?
  val macAddress: String?
  val batteryLevelForPrintable: Int?
  val batteryPoweredForPrintable: Boolean?
  val wifiRssiForPrintable: Int?
  val wifiSignalStrengthForPrintable: Int?
  val bridgeNodeOnlineForPrintable: Boolean?
  val bridgeNodeSignalStrengthForPrintable: Int?
  val uptimeForPrintable: Int?
  val connectionUptimeForPrintable: Int?
  val batteryHealthForPrintable: Int?
  val lastConnectionResetCauseForPrintable: Int?
  val switchCycleCountForPrintable: Int?
  val lightSourceLifespanForPrintable: Int?
  val lightSourceLifespanLeftForPrintable: Float?
  val lightSourceOperatingTimeForPrintable: Int?
}

val SuplaChannelStatePrintable.channelIdString: LocalizedString?
  get() = LocalizedString.Constant("$channelId")

val SuplaChannelStatePrintable.batteryLevelString: LocalizedString?
  get() = percentageString(batteryLevelForPrintable)

val SuplaChannelStatePrintable.batteryPoweredString: LocalizedString?
  get() = batteryPoweredString(batteryPoweredForPrintable)

val SuplaChannelStatePrintable.wifiRssiString: LocalizedString?
  get() = wifiRssiString(wifiRssiForPrintable)

val SuplaChannelStatePrintable.wifiSignalStrengthString: LocalizedString?
  get() = percentageString(wifiSignalStrengthForPrintable)

val SuplaChannelStatePrintable.bridgeNodeOnlineString: LocalizedString?
  get() = bridgeNodeOnlineForPrintable?.localizedString

val SuplaChannelStatePrintable.bridgeNodeSignalStrengthString: LocalizedString?
  get() = percentageString(bridgeNodeSignalStrengthForPrintable)

val SuplaChannelStatePrintable.uptimeString: LocalizedString?
  get() = uptimeString(uptimeForPrintable)

val SuplaChannelStatePrintable.connectionUptimeString: LocalizedString?
  get() = uptimeString(connectionUptimeForPrintable)

val SuplaChannelStatePrintable.batteryHealthString: LocalizedString?
  get() = percentageString(batteryHealthForPrintable)

val SuplaChannelStatePrintable.lastConnectionResetCauseString: LocalizedString?
  get() = resetCauseString(lastConnectionResetCauseForPrintable)

val SuplaChannelStatePrintable.switchCycleCountString: LocalizedString?
  get() = switchCycleCountForPrintable?.let { LocalizedString.Constant("$it") }

val SuplaChannelStatePrintable.lightSourceOperatingTimePercent: Float?
  get() = lightSourceOperatingTimeForPrintable?.let { lightSourceOperatingTime ->
    lightSourceLifespanForPrintable?.let { lightSourceLifespan ->
      (lightSourceLifespan > 0).ifTrue { lightSourceOperatingTime / 36f / lightSourceLifespan }
    }
  }

val SuplaChannelStatePrintable.lightSourceOperatingTimePercentLeft: Float?
  get() = lightSourceOperatingTimePercent?.let { 100 - it }

private fun resetCauseString(cause: Int?): LocalizedString? =
  cause?.let {
    val causeResources = listOf(
      LocalizedStringId.LAST_CONNECTION_RESET_CAUSE_UNKNOWN,
      LocalizedStringId.LAST_CONNECTION_RESET_CAUSE_ACTIVITY_TIMEOUT,
      LocalizedStringId.LAST_CONNECTION_RESET_CAUSE_WIFI_CONNECTION_LOST,
      LocalizedStringId.LAST_CONNECTION_RESET_CAUSE_SERVER_CONNECTION_LOST
    )
    causeResources.getOrNull(cause)?.let { localizedString(it) } ?: LocalizedString.Constant("$cause")
  }

private fun percentageString(batteryLevel: Int?): LocalizedString? =
  batteryLevel?.let { LocalizedString.Constant("$it%") }

private fun batteryPoweredString(batteryPowered: Boolean?): LocalizedString? =
  batteryPowered?.let {
    localizedString(if (it) LocalizedStringId.CHANNEL_STATE_BATTERY_POWERED else LocalizedStringId.CHANNEL_STATE_MAINS_POWERED)
  }

private fun wifiRssiString(wifiRssi: Int?): LocalizedString? =
  wifiRssi?.let { LocalizedString.Constant("$it dBm") }

private fun uptimeString(time: Int?): LocalizedString? =
  time?.let {
    localizedString(
      LocalizedStringId.CHANNEL_STATE_UPTIME,
      arg1 = it / 86400,
      arg2 = it.mod(86400) / 3600,
      arg3 = it.mod(3600) / 60,
      arg4 = it.mod(60)
    )
  }
