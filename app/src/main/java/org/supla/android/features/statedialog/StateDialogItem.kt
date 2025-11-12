package org.supla.android.features.statedialog
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
import org.supla.android.lib.SuplaChannelStatePrintable
import org.supla.android.lib.batteryHealthString
import org.supla.android.lib.batteryLevelString
import org.supla.android.lib.batteryPoweredString
import org.supla.android.lib.bridgeNodeOnlineString
import org.supla.android.lib.bridgeNodeSignalStrengthString
import org.supla.android.lib.channelIdString
import org.supla.android.lib.connectionUptimeString
import org.supla.android.lib.lastConnectionResetCauseString
import org.supla.android.lib.lightSourceOperatingTimePercentLeft
import org.supla.android.lib.switchCycleCountString
import org.supla.android.lib.uptimeString
import org.supla.android.lib.wifiRssiString
import org.supla.android.lib.wifiSignalStrengthString
import org.supla.core.shared.infrastructure.LocalizedString
import java.util.Locale

enum class StateDialogItem(val captionResource: Int) {
  CHANNEL_ID(R.string.channel_id),
  IP_ADDRESS(R.string.IP),
  MAC_ADDRESS(R.string.MAC),
  BATTERY_LEVEL(R.string.battery_level),
  POWER_SUPPLY(R.string.state_power_supply),
  WIFI_RSSI(R.string.wifi_rssi),
  WIFI_SIGNAL(R.string.wifi_signal_strength),
  BRIDGE_NODE(R.string.bridge_node_online),
  BRIDGE_SIGNAL(R.string.bridge_signal_strength),
  UPTIME(R.string.uptime),
  CONNECTION_TIME(R.string.connection_time),
  BATTERY_HEALTH(R.string.battery_health),
  CONNECTION_RESET(R.string.connection_reset_cause),
  SWITCH_CYCLE_COUNT(R.string.state_switch_cycle_count),
  LIGHT_SOURCE_LIFESPAN(R.string.light_source_lifespan),
  LIGHT_SOURCE_OPERATING_TIME(R.string.light_source_operatingtime);

  val extractor: (SuplaChannelStatePrintable) -> LocalizedString?
    get() = when (this) {
      CHANNEL_ID -> { state -> state.channelIdString }
      IP_ADDRESS -> { state -> state.ipV4?.let { LocalizedString.Constant(it) } }
      MAC_ADDRESS -> { state -> state.macAddress?.let { LocalizedString.Constant(it) } }
      BATTERY_LEVEL -> { state -> state.batteryLevelString }
      POWER_SUPPLY -> { state -> state.batteryPoweredString }
      WIFI_RSSI -> { state -> state.wifiRssiString }
      WIFI_SIGNAL -> { state -> state.wifiSignalStrengthString }
      BRIDGE_NODE -> { state -> state.bridgeNodeOnlineString }
      BRIDGE_SIGNAL -> { state -> state.bridgeNodeSignalStrengthString }
      UPTIME -> { state -> state.uptimeString }
      CONNECTION_TIME -> { state -> state.connectionUptimeString }
      BATTERY_HEALTH -> { state -> state.batteryHealthString }
      CONNECTION_RESET -> { state -> state.lastConnectionResetCauseString }
      SWITCH_CYCLE_COUNT -> { state -> state.switchCycleCountString }
      LIGHT_SOURCE_LIFESPAN -> { state -> state.lightSourceLifespanString }
      LIGHT_SOURCE_OPERATING_TIME -> { state -> state.lightSourceOperatingTimeString }
    }

  private val SuplaChannelStatePrintable.lightSourceLifespanString: LocalizedString?
    get() = lightSourceLifespanForPrintable?.let { lightSourceLifespan ->
      val left = lightSourceLifespanLeftForPrintable ?: lightSourceOperatingTimePercentLeft

      val string = if (left != null) {
        String.format(Locale.getDefault(), "%dh (%.2f%%)", lightSourceLifespan, left)
      } else {
        String.format(Locale.getDefault(), "%dh", lightSourceLifespan)
      }

      LocalizedString.Constant(string)
    }

  val SuplaChannelStatePrintable.lightSourceOperatingTimeString: LocalizedString?
    get() = lightSourceOperatingTimeForPrintable?.let {
      val string = String.format(Locale.getDefault(), "%02dh %02d:%02d", it / 3600, it % 3600 / 60, it % 60)
      LocalizedString.Constant(string)
    }

  companion object {
    fun values(state: SuplaChannelStatePrintable): Map<StateDialogItem, LocalizedString> =
      entries.associateWith { it.extractor(state) }
        .filter { it.value != null }
        .mapValues { it.value!! }
  }
}
