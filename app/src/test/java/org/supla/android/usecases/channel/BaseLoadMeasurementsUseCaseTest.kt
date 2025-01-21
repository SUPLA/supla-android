package org.supla.android.usecases.channel
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

import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeasurementEntity
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.local.entity.measurements.TemperatureLogEntity
import org.supla.android.extensions.date
import java.util.Calendar
import java.util.Date

internal const val MINUTE_IN_MILLIS = 60000

open class BaseLoadMeasurementsUseCaseTest {
  protected fun mockEntities(count: Int, timeDistance: Int, channelId: Int = 123): List<TemperatureLogEntity> =
    mutableListOf<TemperatureLogEntity>().apply {
      val initialTime = date(2022, Calendar.NOVEMBER, 11).time

      for (i in 0..count) {
        add(
          TemperatureLogEntity(
            id = null,
            channelId = channelId,
            date = Date(initialTime.plus(timeDistance * i)),
            temperature = i.toFloat(),
            groupingString = "",
            profileId = 1L
          )
        )
      }
    }

  protected fun mockEntitiesWithHumidity(count: Int, timeDistance: Int, channelId: Int = 123): List<TemperatureAndHumidityLogEntity> =
    mutableListOf<TemperatureAndHumidityLogEntity>().apply {
      val initialTime = date(2022, 10, 11).time

      for (i in 0..count) {
        add(
          TemperatureAndHumidityLogEntity(
            id = null,
            channelId = channelId,
            date = Date(initialTime.plus(timeDistance * i)),
            temperature = i.toFloat(),
            humidity = (count - i).toFloat(),
            groupingString = "",
            profileId = 1L
          )
        )
      }
    }

  protected fun mockGpMeterEntities(count: Int, timeDistance: Int, channelId: Int = 123): List<GeneralPurposeMeterEntity> =
    mutableListOf<GeneralPurposeMeterEntity>().apply {
      val initialTime = date(2022, 10, 11).time

      for (i in 0..count) {
        add(
          GeneralPurposeMeterEntity(
            id = null,
            channelId = channelId,
            date = Date(initialTime.plus(timeDistance * i)),
            value = i.toFloat(),
            valueIncrement = i.toFloat(),
            counterReset = false,
            manuallyComplemented = false,
            groupingString = "",
            profileId = 1L
          )
        )
      }
    }

  protected fun mockGpMeasurementEntities(count: Int, timeDistance: Int, channelId: Int = 123): List<GeneralPurposeMeasurementEntity> =
    mutableListOf<GeneralPurposeMeasurementEntity>().apply {
      val initialTime = date(2022, 10, 11).time

      for (i in 0..count) {
        add(
          GeneralPurposeMeasurementEntity(
            id = null,
            channelId = channelId,
            date = Date(initialTime.plus(timeDistance * i)),
            valueAverage = i.toFloat(),
            valueOpen = 0f,
            valueClose = count.toFloat(),
            valueMin = 0f,
            valueMax = count.toFloat(),
            groupingString = "",
            profileId = 1L
          )
        )
      }
    }
}
