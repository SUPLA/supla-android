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

@Entity(
  tableName = ChannelRelationEntity.TABLE_NAME,
  primaryKeys = [ChannelRelationEntity.COLUMN_CHANNEL_ID, ChannelRelationEntity.COLUMN_PARENT_ID, ChannelRelationEntity.COLUMN_PROFILE_ID]
)
data class ChannelRelationEntity(
  @ColumnInfo(name = COLUMN_CHANNEL_ID) val channelId: Int,
  @ColumnInfo(name = COLUMN_PARENT_ID) val parentId: Int,
  @ColumnInfo(name = COLUMN_CHANNEL_RELATION_TYPE) val channelRelationType: ChannelRelationType,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long,
  @ColumnInfo(name = COLUMN_DELETE_FLAG) val deleteFlag: Boolean
) {
  companion object {
    const val TABLE_NAME = "channel_relation"
    const val COLUMN_CHANNEL_ID = "channel_id"
    const val COLUMN_PARENT_ID = "parent_id"
    const val COLUMN_CHANNEL_RELATION_TYPE = "channel_relation_type"
    const val COLUMN_PROFILE_ID = "profileid" // Needs to be without underscore because of other tables
    const val COLUMN_DELETE_FLAG = "delete_flag"
  }
}

enum class ChannelRelationType(val value: Short) {
  DEFAULT(0),
  OPENING_SENSOR(1),
  PARTIAL_OPENING_SENSOR(2),
  METER(3),
  MAIN_THERMOMETER(4),
  AUX_THERMOMETER_FLOOR(5),
  AUX_THERMOMETER_WATER(6),
  AUX_THERMOMETER_GENERIC_HEATER(7),
  AUX_THERMOMETER_GENERIC_COOLER(8);

  fun isThermometer(): Boolean {
    return isMainThermometer() || isAuxThermometer()
  }

  fun isAuxThermometer(): Boolean {
    return when (this) {
      AUX_THERMOMETER_FLOOR,
      AUX_THERMOMETER_WATER,
      AUX_THERMOMETER_GENERIC_HEATER,
      AUX_THERMOMETER_GENERIC_COOLER -> true
      else -> false
    }
  }

  fun isMainThermometer(): Boolean {
    return when (this) {
      MAIN_THERMOMETER -> true
      else -> false
    }
  }

  companion object {
    fun from(value: Short): ChannelRelationType {
      for (type in ChannelRelationType.values()) {
        if (type.value == value) {
          return type
        }
      }

      throw IllegalArgumentException("Illegal channel relation type value: $value")
    }
  }
}
