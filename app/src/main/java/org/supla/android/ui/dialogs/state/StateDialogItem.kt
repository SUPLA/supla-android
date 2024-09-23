package org.supla.android.ui.dialogs.state
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
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.stringProvider
import org.supla.android.core.ui.stringProviderOf
import org.supla.android.extensions.ifTrue
import org.supla.android.lib.SuplaChannelState

enum class StateDialogItem(val captionResource: Int) {
  CHANNEL_ID(R.string.channel_id),
  IP_ADDRESS(R.string.IP),
  MAC_ADDRESS(R.string.MAC),
  BATTERY_LEVEL(R.string.battery_level),
  BATTERY_POWERED(R.string.battery_powered),
  WIFI_RSSI(R.string.wifi_rssi),
  WIFI_SIGNAL(R.string.wifi_signal_strength),
  BRIDGE_NODE(R.string.bridge_node_online),
  BRIDGE_SIGNAL(R.string.bridge_signal_strength),
  UPTIME(R.string.uptime),
  CONNECTION_TIME(R.string.connection_time),
  BATTERY_HEALTH(R.string.battery_health),
  CONNECTION_RESET(R.string.connection_reset_cause),
  LIGHT_SOURCE_LIFESPAN(R.string.light_source_lifespan),
  LIGHT_SOURCE_OPERATING_TIME(R.string.light_source_operatingtime);

  val extractor: (SuplaChannelState) -> StringProvider?
    get() = when (this) {
      CHANNEL_ID -> { state -> stringProviderOf("${state.channelID}") }
      IP_ADDRESS -> { state -> (state.ipv4 != null).ifTrue { stringProviderOf(state.ipv4String) } }
      MAC_ADDRESS -> { state -> (state.macAddress != null).ifTrue { stringProviderOf(state.macAddressString) } }
      BATTERY_LEVEL -> { state -> (state.batteryLevel != null).ifTrue { stringProviderOf(state.batteryLevelString) } }
      BATTERY_POWERED -> { state -> state.isBatteryPowered?.stringProvider }

      WIFI_RSSI -> { state -> (state.wiFiRSSI != null).ifTrue { stringProviderOf(state.wiFiRSSIString) } }
      WIFI_SIGNAL -> { state -> (state.wiFiSignalStrength != null).ifTrue { stringProviderOf(state.wiFiSignalStrengthString) } }
      BRIDGE_NODE -> { state -> state.isBridgeNodeOnline?.stringProvider }
      BRIDGE_SIGNAL -> { state -> state.bridgeNodeSignalStrengthStringProvider }
      UPTIME -> { state -> state.uptimeStringProvider }
      CONNECTION_TIME -> { state -> state.connectionUptimeStringProvider }
      BATTERY_HEALTH -> { state -> (state.batteryHealth != null).ifTrue { stringProviderOf(state.batteryHealthString) } }
      CONNECTION_RESET -> { state -> state.lastConnectionResetCauseStringProvider }
      else -> { _ -> null }
    }

  private val Boolean.stringProvider: StringProvider
    get() = if (this) stringProviderOf(R.string.yes) else stringProviderOf(R.string.no)

  private val SuplaChannelState.bridgeNodeSignalStrengthStringProvider: StringProvider?
    get() = (bridgeNodeSignalStrength != null).ifTrue { stringProviderOf(bridgeNodeSignalStrengthString) }

  private val SuplaChannelState.uptimeStringProvider: StringProvider?
    get() = (uptime != null).ifTrue { stringProvider { getUptimeString(it) } }

  private val SuplaChannelState.connectionUptimeStringProvider: StringProvider?
    get() = (connectionUptime != null).ifTrue { stringProvider { getConnectionUptimeString(it) } }

  private val SuplaChannelState.lastConnectionResetCauseStringProvider: StringProvider?
    get() = lastConnectionResetCause?.let { cause ->
      val causeResources = listOf(
        R.string.lastconnectionresetcause_unknown,
        R.string.lastconnectionresetcause_activity_timeout,
        R.string.lastconnectionresetcause_wifi_connection_lost,
        R.string.lastconnectionresetcause_server_connection_lost
      )

      causeResources.getOrNull(cause.toInt())?.let {
        stringProviderOf(it)
      } ?: stringProviderOf("$cause")
    }
}
