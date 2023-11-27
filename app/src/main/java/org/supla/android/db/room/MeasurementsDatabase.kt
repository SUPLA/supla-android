package org.supla.android.db.room
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

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.supla.android.data.source.local.RoomTemperatureAndHumidityLogDao
import org.supla.android.data.source.local.RoomTemperatureLogDao
import org.supla.android.data.source.local.entity.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.local.entity.TemperatureLogEntity
import org.supla.android.db.DbHelper

@Database(
  entities = [TemperatureLogEntity::class, TemperatureAndHumidityLogEntity::class],
  version = DbHelper.DATABASE_VERSION,
  exportSchema = false
)
@TypeConverters(MeasurementsDatabaseConverters::class)
abstract class MeasurementsDatabase : RoomDatabase() {
  abstract fun temperatureLogDao(): RoomTemperatureLogDao

  abstract fun temperatureAndHumidityLogDao(): RoomTemperatureAndHumidityLogDao
}
