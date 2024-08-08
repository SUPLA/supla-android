package org.supla.android.db.room.measurements
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
import org.supla.android.data.source.local.dao.measurements.ElectricityMeterLogDao
import org.supla.android.data.source.local.dao.measurements.GeneralPurposeMeasurementLogDao
import org.supla.android.data.source.local.dao.measurements.GeneralPurposeMeterLogDao
import org.supla.android.data.source.local.dao.measurements.TemperatureAndHumidityLogDao
import org.supla.android.data.source.local.dao.measurements.TemperatureLogDao
import org.supla.android.data.source.local.entity.measurements.CurrentHistoryLogEntity
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeasurementEntity
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity
import org.supla.android.data.source.local.entity.measurements.HomePlusThermostatLogEntity
import org.supla.android.data.source.local.entity.measurements.ImpulseCounterLogEntity
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.local.entity.measurements.TemperatureLogEntity
import org.supla.android.data.source.local.entity.measurements.VoltageHistoryLogEntity
import org.supla.android.db.MeasurementsDbHelper

@Database(
  entities = [
    TemperatureLogEntity::class,
    TemperatureAndHumidityLogEntity::class,
    GeneralPurposeMeasurementEntity::class,
    GeneralPurposeMeterEntity::class,
    ElectricityMeterLogEntity::class,
    ImpulseCounterLogEntity::class,
    HomePlusThermostatLogEntity::class,
    VoltageHistoryLogEntity::class,
    CurrentHistoryLogEntity::class
  ],
  version = MeasurementsDbHelper.DATABASE_VERSION,
  exportSchema = false,
)
@TypeConverters(MeasurementsDatabaseConverters::class)
abstract class MeasurementsDatabase : RoomDatabase() {
  abstract fun temperatureLogDao(): TemperatureLogDao

  abstract fun temperatureAndHumidityLogDao(): TemperatureAndHumidityLogDao

  abstract fun generalPurposeMeterLogDao(): GeneralPurposeMeterLogDao

  abstract fun generalPurposeMeasurementLogDao(): GeneralPurposeMeasurementLogDao

  abstract fun electricityMeterLogDao(): ElectricityMeterLogDao
}
