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
import com.google.gson.Gson
import org.supla.android.data.source.local.entity.ChannelConfigEntity.Companion.COLUMN_CHANNEL_ID
import org.supla.android.data.source.local.entity.ChannelConfigEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ChannelConfigEntity.Companion.TABLE_NAME
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.android.data.source.remote.container.SuplaChannelContainerConfig
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeMeasurementConfig
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeMeterConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelHvacConfig
import org.supla.android.data.source.remote.rollershutter.SuplaChannelFacadeBlindConfig

@Entity(
  tableName = TABLE_NAME,
  primaryKeys = [COLUMN_CHANNEL_ID, COLUMN_PROFILE_ID]
)
data class ChannelConfigEntity(
  @ColumnInfo(name = COLUMN_CHANNEL_ID) val channelId: Int,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long,
  @ColumnInfo(name = COLUMN_CONFIG) val config: String,
  @ColumnInfo(name = COLUMN_CONFIG_TYPE) val configType: ChannelConfigType,
  @ColumnInfo(name = COLUMN_CONFIG_CRC32) val configCrc32: Long
) {

  fun toSuplaConfig(gson: Gson): SuplaChannelConfig =
    when (configType) {
      ChannelConfigType.GENERAL_PURPOSE_MEASUREMENT ->
        gson.fromJson(config, SuplaChannelGeneralPurposeMeasurementConfig::class.java)

      ChannelConfigType.GENERAL_PURPOSE_METER ->
        gson.fromJson(config, SuplaChannelGeneralPurposeMeterConfig::class.java)

      ChannelConfigType.HVAC ->
        gson.fromJson(config, SuplaChannelHvacConfig::class.java)

      ChannelConfigType.CONTAINER ->
        gson.fromJson(config, SuplaChannelContainerConfig::class.java)

      ChannelConfigType.FACADE_BLIND ->
        gson.fromJson(config, SuplaChannelFacadeBlindConfig::class.java)

      ChannelConfigType.DEFAULT,
      ChannelConfigType.WEEKLY_SCHEDULE,
      ChannelConfigType.UNKNOWN ->
        gson.fromJson(config, SuplaChannelConfig::class.java)
    }

  companion object {
    const val TABLE_NAME = "channel_config"
    const val COLUMN_CHANNEL_ID = "channel_id"
    const val COLUMN_PROFILE_ID = "profileid" // Needs to be without underscore because of other tables
    const val COLUMN_CONFIG = "config"
    const val COLUMN_CONFIG_TYPE = "config_type"
    const val COLUMN_CONFIG_CRC32 = "config_crc32"

    const val SQL = """
      CREATE TABLE $TABLE_NAME
      (
        $COLUMN_CHANNEL_ID INTEGER NOT NULL,
        $COLUMN_PROFILE_ID INTEGER NOT NULL,
        $COLUMN_CONFIG TEXT NOT NULL,
        $COLUMN_CONFIG_TYPE INTEGER NOT NULL,
        $COLUMN_CONFIG_CRC32 INTEGER NOT NULL,
        PRIMARY KEY ($COLUMN_CHANNEL_ID, $COLUMN_PROFILE_ID)
      )
    """

    const val ALL_COLUMNS = "$COLUMN_CHANNEL_ID, $COLUMN_PROFILE_ID, $COLUMN_CONFIG, $COLUMN_CONFIG_TYPE, $COLUMN_CONFIG_CRC32"
  }
}
