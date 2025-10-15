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
import org.supla.android.lib.SuplaChannelState
import org.supla.android.lib.lightSourceLifespanString
import org.supla.android.lib.lightSourceOperatingTimeString
import org.supla.core.shared.extensions.localizedString
import org.supla.core.shared.infrastructure.LocalizedString

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

  val extractor: (SuplaChannelState) -> LocalizedString?
    get() = when (this) {
      CHANNEL_ID -> { state -> LocalizedString.Constant("${state.channelId}") }
      IP_ADDRESS -> { state -> state.ipV4?.let { LocalizedString.Constant(it) } }
      MAC_ADDRESS -> { state -> state.macAddress?.let { LocalizedString.Constant(it) } }
      BATTERY_LEVEL -> { state -> state.batteryLevelString?.let { LocalizedString.Constant(it) } }
      POWER_SUPPLY -> { state -> state.batteryPoweredString }

      WIFI_RSSI -> { state -> state.wifiRssiString?.let { LocalizedString.Constant(it) } }
      WIFI_SIGNAL -> { state -> state.wifiSignalStrengthString?.let { LocalizedString.Constant(it) } }
      BRIDGE_NODE -> { state -> state.bridgeNodeOnline?.localizedString }
      BRIDGE_SIGNAL -> { state -> state.bridgeNodeSignalStrengthString?.let { LocalizedString.Constant(it) } }
      UPTIME -> { state -> state.uptimeString }
      CONNECTION_TIME -> { state -> state.connectionUptimeString }
      BATTERY_HEALTH -> { state -> state.batteryHealthString?.let { LocalizedString.Constant(it) } }
      CONNECTION_RESET -> { state -> state.lastConnectionResetCauseString }
      SWITCH_CYCLE_COUNT -> { state -> state.switchCycleCountString?.let { LocalizedString.Constant(it) } }
      LIGHT_SOURCE_LIFESPAN -> { state -> state.lightSourceLifespanString?.let { LocalizedString.Constant(it) } }
      LIGHT_SOURCE_OPERATING_TIME -> { state -> state.lightSourceOperatingTimeString?.let { LocalizedString.Constant(it) } }
    }
}
