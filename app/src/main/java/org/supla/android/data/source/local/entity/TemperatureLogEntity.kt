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
import androidx.room.Index
import androidx.room.PrimaryKey
import org.supla.android.db.SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID
import org.supla.android.db.SuplaContract.TemperatureLogEntry.COLUMN_NAME_PROFILEID
import org.supla.android.db.SuplaContract.TemperatureLogEntry.COLUMN_NAME_TEMPERATURE
import org.supla.android.db.SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP
import org.supla.android.db.SuplaContract.TemperatureLogEntry.TABLE_NAME
import org.supla.android.db.SuplaContract.TemperatureLogEntry._ID
import java.util.Date

@Entity(
  tableName = TABLE_NAME,
  indices = [
    Index(
      value = [ COLUMN_NAME_CHANNELID ],
      name = "${TABLE_NAME}_${COLUMN_NAME_CHANNELID}_index"
    ),
    Index(
      value = [ COLUMN_NAME_TIMESTAMP ],
      name = "${TABLE_NAME}_${COLUMN_NAME_TIMESTAMP}_index"
    ),
    Index(
      value = [ COLUMN_NAME_CHANNELID, COLUMN_NAME_TIMESTAMP, COLUMN_NAME_PROFILEID ],
      name = "${TABLE_NAME}_unique_index",
      unique = true
    )
  ]
)
data class TemperatureLogEntity(
  @PrimaryKey
  @ColumnInfo(name = _ID)
  val id: Long?,
  @ColumnInfo(name = COLUMN_NAME_CHANNELID) val channelId: Int,
  @ColumnInfo(name = COLUMN_NAME_TIMESTAMP) override val date: Date,
  @ColumnInfo(name = COLUMN_NAME_TEMPERATURE) override val temperature: Float?,
  @ColumnInfo(name = COLUMN_NAME_PROFILEID) val profileId: Long
) : BaseTemperatureEntity
