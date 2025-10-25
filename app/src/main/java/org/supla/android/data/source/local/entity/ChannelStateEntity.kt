package org.supla.android.data.source.local.entity
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

import androidx.room.ColumnInfo
import androidx.room.Entity
import org.supla.android.lib.SuplaChannelStatePrintable
import org.supla.core.shared.data.model.battery.BatteryInfo
import org.supla.core.shared.extensions.ifTrue

@Entity(
  tableName = ChannelStateEntity.TABLE_NAME,
  primaryKeys = [ChannelStateEntity.COLUMN_CHANNEL_ID, ChannelStateEntity.COLUMN_PROFILE_ID]
)
data class ChannelStateEntity(
  @ColumnInfo(name = COLUMN_BATTERY_HEALTH) val batteryHealth: Int?,
  @ColumnInfo(name = COLUMN_BATTERY_LEVEL) val batteryLevel: Int?,
  @ColumnInfo(name = COLUMN_BATTERY_POWERED) val batteryPowered: Boolean?,
  @ColumnInfo(name = COLUMN_BRIDGE_NODE_ONLINE) val bridgeNodeOnline: Boolean?,
  @ColumnInfo(name = COLUMN_BRIDGE_NODE_SIGNAL_STRENGTH) val bridgeNodeSignalStrength: Int?,
  @ColumnInfo(name = COLUMN_CONNECTION_UPTIME) val connectionUptime: Int?,
  @ColumnInfo(name = COLUMN_IP_V4) override val ipV4: String?,
  @ColumnInfo(name = COLUMN_LAST_CONNECTION_RESET_CAUSE) val lastConnectionResetCause: Int?,
  @ColumnInfo(name = COLUMN_LIGHT_SOURCE_LIFESPAN) val lightSourceLifespan: Int?,
  @ColumnInfo(name = COLUMN_LIGHT_SOURCE_LIFESPAN_LEFT) val lightSourceLifespanLeft: Float?,
  @ColumnInfo(name = COLUMN_LIGHT_SOURCE_OPERATING_TIME) val lightSourceOperatingTime: Int?,
  @ColumnInfo(name = COLUMN_MAC_ADDRESS) override val macAddress: String?,
  @ColumnInfo(name = COLUMN_UPTIME) val uptime: Int?,
  @ColumnInfo(name = COLUMN_WIFI_RSSI) val wifiRssi: Int?,
  @ColumnInfo(name = COLUMN_WIFI_SIGNAL_STRENGTH) val wifiSignalStrength: Int?,
  @ColumnInfo(name = COLUMN_CHANNEL_ID) override val channelId: Int,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long
) : SuplaChannelStatePrintable {

  override val batteryLevelForPrintable: Int?
    get() = batteryLevel
  override val batteryPoweredForPrintable: Boolean?
    get() = batteryPowered
  override val wifiRssiForPrintable: Int?
    get() = wifiRssi
  override val wifiSignalStrengthForPrintable: Int?
    get() = wifiSignalStrength
  override val bridgeNodeOnlineForPrintable: Boolean?
    get() = bridgeNodeOnline
  override val bridgeNodeSignalStrengthForPrintable: Int?
    get() = bridgeNodeSignalStrength
  override val uptimeForPrintable: Int?
    get() = uptime
  override val connectionUptimeForPrintable: Int?
    get() = connectionUptime
  override val batteryHealthForPrintable: Int?
    get() = batteryHealth
  override val lastConnectionResetCauseForPrintable: Int?
    get() = lastConnectionResetCause
  override val switchCycleCountForPrintable: Int?
    get() = null
  override val lightSourceLifespanForPrintable: Int?
    get() = lightSourceLifespan
  override val lightSourceLifespanLeftForPrintable: Float?
    get() = lightSourceLifespanLeft
  override val lightSourceOperatingTimeForPrintable: Int?
    get() = lightSourceOperatingTime

  companion object {
    const val TABLE_NAME = "channel_state"
    const val COLUMN_BATTERY_HEALTH = "battery_health"
    const val COLUMN_BATTERY_LEVEL = "battery_level"
    const val COLUMN_BATTERY_POWERED = "batter_powered"
    const val COLUMN_BRIDGE_NODE_ONLINE = "bridge_node_online"
    const val COLUMN_BRIDGE_NODE_SIGNAL_STRENGTH = "bridge_node_signal_strength"
    const val COLUMN_CONNECTION_UPTIME = "connection_uptime"
    const val COLUMN_IP_V4 = "ip_v4"
    const val COLUMN_LAST_CONNECTION_RESET_CAUSE = "last_connection_reset_cause"
    const val COLUMN_LIGHT_SOURCE_LIFESPAN = "light_source_lifespan"
    const val COLUMN_LIGHT_SOURCE_LIFESPAN_LEFT = "light_source_lifespan_left"
    const val COLUMN_LIGHT_SOURCE_OPERATING_TIME = "light_source_operating_time"
    const val COLUMN_MAC_ADDRESS = "mac_address"
    const val COLUMN_UPTIME = "uptime"
    const val COLUMN_WIFI_RSSI = "wifi_rssi"
    const val COLUMN_WIFI_SIGNAL_STRENGTH = "wifi_signal_strength"
    const val COLUMN_CHANNEL_ID = "channel_id"
    const val COLUMN_PROFILE_ID = "profile_id"

    const val ALL_COLUMNS = "$COLUMN_BATTERY_HEALTH, $COLUMN_BATTERY_LEVEL, $COLUMN_BATTERY_POWERED, " +
      "$COLUMN_BRIDGE_NODE_ONLINE, $COLUMN_BRIDGE_NODE_SIGNAL_STRENGTH, $COLUMN_CONNECTION_UPTIME, " +
      "$COLUMN_IP_V4, $COLUMN_LAST_CONNECTION_RESET_CAUSE, $COLUMN_LIGHT_SOURCE_LIFESPAN, " +
      "$COLUMN_LIGHT_SOURCE_LIFESPAN_LEFT, $COLUMN_LIGHT_SOURCE_OPERATING_TIME, $COLUMN_MAC_ADDRESS, " +
      "$COLUMN_UPTIME, $COLUMN_WIFI_RSSI, $COLUMN_WIFI_SIGNAL_STRENGTH, $COLUMN_CHANNEL_ID, $COLUMN_PROFILE_ID"

    operator fun invoke(channelId: Int, profileId: Long): ChannelStateEntity =
      ChannelStateEntity(
        batteryHealth = null,
        batteryLevel = null,
        batteryPowered = null,
        bridgeNodeOnline = null,
        bridgeNodeSignalStrength = null,
        connectionUptime = null,
        ipV4 = null,
        lastConnectionResetCause = null,
        lightSourceLifespan = null, lightSourceLifespanLeft = null, lightSourceOperatingTime = null,
        macAddress = null,
        uptime = null,
        wifiRssi = null,
        wifiSignalStrength = null,
        channelId = channelId,
        profileId = profileId
      )
  }
}

val ChannelStateEntity.batteryInfo: BatteryInfo?
  get() =
    (batteryPowered != null || batteryLevel != null).ifTrue {
      BatteryInfo(batteryPowered, batteryLevel, batteryHealth)
    }
