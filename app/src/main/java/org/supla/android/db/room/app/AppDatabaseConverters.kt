package org.supla.android.db.room.app
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

import androidx.room.TypeConverter
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.core.shared.data.SuplaChannelFunction
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

class AppDatabaseConverters {
  @TypeConverter
  fun channelRelationTypeFromInt(value: Int?): ChannelRelationType? {
    return value?.let { ChannelRelationType.from(it.toShort()) }
  }

  @TypeConverter
  fun intFromChannelRelationType(value: ChannelRelationType?): Int? {
    return value?.value?.toInt()
  }

  @TypeConverter
  fun channelConfigTypeToInt(config: ChannelConfigType?): Int? {
    return config?.value
  }

  @TypeConverter
  fun intToChannelConfigType(config: Int?): ChannelConfigType? {
    return config?.let { ChannelConfigType.from(it) }
  }

  @TypeConverter
  fun dateToLong(value: Date): Long {
    return value.time
  }

  @TypeConverter
  fun longToDate(value: Long): Date {
    return Date(value)
  }

  @TypeConverter
  fun dateTimeToLong(value: LocalDateTime): Long {
    return value.toEpochSecond(ZoneOffset.UTC)
  }

  @TypeConverter
  fun longToDateTime(value: Long): LocalDateTime {
    return LocalDateTime.ofEpochSecond(value, 0, ZoneOffset.UTC)
  }

  @TypeConverter
  fun suplaFunctionToInt(suplaChannelFunction: SuplaChannelFunction?): Int? =
    suplaChannelFunction?.value

  @TypeConverter
  fun intToSuplaChannelFunction(value: Int?): SuplaChannelFunction? =
    value?.let { SuplaChannelFunction.from(it) }
}
