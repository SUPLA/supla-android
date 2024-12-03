package org.supla.android.db.room.app.migrations
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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_BATTERY_HEALTH
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_BATTERY_LEVEL
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_BATTERY_POWERED
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_BRIDGE_NODE_ONLINE
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_BRIDGE_NODE_SIGNAL_STRENGTH
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_CHANNEL_ID
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_CONNECTION_UPTIME
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_IP_V4
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_LAST_CONNECTION_RESET_CAUSE
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_LIGHT_SOURCE_LIFESPAN
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_LIGHT_SOURCE_LIFESPAN_LEFT
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_LIGHT_SOURCE_OPERATING_TIME
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_MAC_ADDRESS
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_UPTIME
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_WIFI_RSSI
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_WIFI_SIGNAL_STRENGTH
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.TABLE_NAME
import org.supla.android.db.room.SqlExecutor

val MIGRATION_36_37: Migration = object : Migration(36, 37), SqlExecutor {

  private val CREATE_CHANNEL_STATE_TABLE_SQL =
    """
      CREATE TABLE $TABLE_NAME 
      (
        $COLUMN_BATTERY_HEALTH INTEGER,
        $COLUMN_BATTERY_LEVEL INTEGER,
        $COLUMN_BATTERY_POWERED INTEGER,
        $COLUMN_BRIDGE_NODE_ONLINE INTEGER,
        $COLUMN_BRIDGE_NODE_SIGNAL_STRENGTH INTEGER,
        $COLUMN_CONNECTION_UPTIME INTEGER,
        $COLUMN_IP_V4 TEXT,
        $COLUMN_LAST_CONNECTION_RESET_CAUSE INTEGER,
        $COLUMN_LIGHT_SOURCE_LIFESPAN INTEGER,
        $COLUMN_LIGHT_SOURCE_LIFESPAN_LEFT INTEGER,
        $COLUMN_LIGHT_SOURCE_OPERATING_TIME REAL,
        $COLUMN_MAC_ADDRESS TEXT,
        $COLUMN_UPTIME INTEGER,
        $COLUMN_WIFI_RSSI INTEGER,
        $COLUMN_WIFI_SIGNAL_STRENGTH INTEGER,
        $COLUMN_CHANNEL_ID INTEGER NOT NULL,
        $COLUMN_PROFILE_ID INTEGER NOT NULL,
        PRIMARY KEY ($COLUMN_CHANNEL_ID, $COLUMN_PROFILE_ID)
      )
    """.trimIndent()

  override fun migrate(database: SupportSQLiteDatabase) {
    createChannelRelationTable(database)
  }

  private fun createChannelRelationTable(db: SupportSQLiteDatabase) {
    execSQL(db, CREATE_CHANNEL_STATE_TABLE_SQL)
  }
}
