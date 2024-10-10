package org.supla.android.data.source.local.entity.measurements
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
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.source.local.entity.custom.EnergyType
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_CHANNEL_ID
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_MANUALLY_COMPLEMENTED
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_TIMESTAMP
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.TABLE_NAME
import org.supla.android.data.source.remote.rest.channel.ElectricityMeasurement
import org.supla.android.features.details.electricitymeterdetail.history.ElectricityMeterChartType
import org.supla.android.usecases.channel.measurementsprovider.electricity.ElectricityChartFilters
import java.util.Date
import kotlin.math.abs
import kotlin.math.max

@Entity(
  tableName = TABLE_NAME,
  indices = [
    Index(
      value = [COLUMN_CHANNEL_ID],
      name = "${TABLE_NAME}_${COLUMN_CHANNEL_ID}_index"
    ),
    Index(
      value = [COLUMN_TIMESTAMP],
      name = "${TABLE_NAME}_${COLUMN_TIMESTAMP}_index"
    ),
    Index(
      value = [COLUMN_MANUALLY_COMPLEMENTED],
      name = "${TABLE_NAME}_${COLUMN_MANUALLY_COMPLEMENTED}_index"
    ),
    Index(
      value = [COLUMN_CHANNEL_ID, COLUMN_TIMESTAMP, COLUMN_PROFILE_ID],
      name = "${TABLE_NAME}_unique_index",
      unique = true
    )
  ]
)
data class ElectricityMeterLogEntity(
  @PrimaryKey
  @ColumnInfo(name = COLUMN_ID)
  val id: Long?,
  @ColumnInfo(name = COLUMN_CHANNEL_ID) val channelId: Int,
  @ColumnInfo(name = COLUMN_TIMESTAMP) override val date: Date,
  @ColumnInfo(name = COLUMN_PHASE1_FAE) val phase1Fae: Float?,
  @ColumnInfo(name = COLUMN_PHASE1_RAE) val phase1Rae: Float?,
  @ColumnInfo(name = COLUMN_PHASE1_FRE) val phase1Fre: Float?,
  @ColumnInfo(name = COLUMN_PHASE1_RRE) val phase1Rre: Float?,
  @ColumnInfo(name = COLUMN_PHASE2_FAE) val phase2Fae: Float?,
  @ColumnInfo(name = COLUMN_PHASE2_RAE) val phase2Rae: Float?,
  @ColumnInfo(name = COLUMN_PHASE2_FRE) val phase2Fre: Float?,
  @ColumnInfo(name = COLUMN_PHASE2_RRE) val phase2Rre: Float?,
  @ColumnInfo(name = COLUMN_PHASE3_FAE) val phase3Fae: Float?,
  @ColumnInfo(name = COLUMN_PHASE3_RAE) val phase3Rae: Float?,
  @ColumnInfo(name = COLUMN_PHASE3_FRE) val phase3Fre: Float?,
  @ColumnInfo(name = COLUMN_PHASE3_RRE) val phase3Rre: Float?,
  @ColumnInfo(name = COLUMN_FAE_BALANCED) val faeBalanced: Float?,
  @ColumnInfo(name = COLUMN_RAE_BALANCED) val raeBalanced: Float?,
  @ColumnInfo(name = COLUMN_MANUALLY_COMPLEMENTED) val manuallyComplemented: Boolean,
  @ColumnInfo(name = COLUMN_COUNTER_RESET) val counterReset: Boolean,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long
) : BaseLogEntity {

  val phase1: PhaseValues
    get() = PhaseValues(phase1Fae, phase1Rae, phase1Fre, phase1Rre)

  val phase2: PhaseValues
    get() = PhaseValues(phase2Fae, phase2Rae, phase2Fre, phase2Rre)

  val phase3: PhaseValues
    get() = PhaseValues(phase3Fae, phase3Rae, phase3Fre, phase3Rre)

  companion object {
    const val TABLE_NAME = "em_log"
    const val COLUMN_ID = "_id"
    const val COLUMN_CHANNEL_ID = "channelid"
    const val COLUMN_TIMESTAMP = "date"
    const val COLUMN_PHASE1_FAE = "phase1_fae"
    const val COLUMN_PHASE1_RAE = "phase1_rae"
    const val COLUMN_PHASE1_FRE = "phase1_fre"
    const val COLUMN_PHASE1_RRE = "phase1_rre"
    const val COLUMN_PHASE2_FAE = "phase2_fae"
    const val COLUMN_PHASE2_RAE = "phase2_rae"
    const val COLUMN_PHASE2_FRE = "phase2_fre"
    const val COLUMN_PHASE2_RRE = "phase2_rre"
    const val COLUMN_PHASE3_FAE = "phase3_fae"
    const val COLUMN_PHASE3_RAE = "phase3_rae"
    const val COLUMN_PHASE3_FRE = "phase3_fre"
    const val COLUMN_PHASE3_RRE = "phase3_rre"
    const val COLUMN_FAE_BALANCED = "fae_balanced"
    const val COLUMN_RAE_BALANCED = "rae_balanced"
    const val COLUMN_MANUALLY_COMPLEMENTED = "complement"
    const val COLUMN_COUNTER_RESET = "counter_reset"
    const val COLUMN_PROFILE_ID = "profileid"

    const val ALL_COLUMNS = "$COLUMN_ID, $COLUMN_CHANNEL_ID, $COLUMN_TIMESTAMP, $COLUMN_PHASE1_FAE, $COLUMN_PHASE1_RAE, " +
      "$COLUMN_PHASE1_FRE, $COLUMN_PHASE1_RRE, $COLUMN_PHASE2_FAE, $COLUMN_PHASE2_RAE, $COLUMN_PHASE2_FRE, $COLUMN_PHASE2_RRE, " +
      "$COLUMN_PHASE3_FAE, $COLUMN_PHASE3_RAE, $COLUMN_PHASE3_FRE, $COLUMN_PHASE3_RRE, $COLUMN_FAE_BALANCED, " +
      "$COLUMN_RAE_BALANCED, $COLUMN_MANUALLY_COMPLEMENTED, $COLUMN_COUNTER_RESET, $COLUMN_PROFILE_ID"

    fun create(
      entry: ElectricityMeasurement,
      channelId: Int,
      profileId: Long,
      id: Long? = null,
      date: Date? = null,
      electricityMeterDiff: ElectricityMeterDiff? = null,
      manuallyComplemented: Boolean = false,
      counterReset: Boolean = false
    ) = ElectricityMeterLogEntity(
      id = id,
      channelId = channelId,
      date = date ?: entry.date,
      phase1Fae = electricityMeterDiff?.phase1?.fae ?: entry.phase1Fae?.toKWh(),
      phase1Rae = electricityMeterDiff?.phase1?.rae ?: entry.phase1Rae?.toKWh(),
      phase1Fre = electricityMeterDiff?.phase1?.fre ?: entry.phase1Fre?.toKWh(),
      phase1Rre = electricityMeterDiff?.phase1?.rre ?: entry.phase1Rre?.toKWh(),
      phase2Fae = electricityMeterDiff?.phase2?.fae ?: entry.phase2Fae?.toKWh(),
      phase2Rae = electricityMeterDiff?.phase2?.rae ?: entry.phase2Rae?.toKWh(),
      phase2Fre = electricityMeterDiff?.phase2?.fre ?: entry.phase2Fre?.toKWh(),
      phase2Rre = electricityMeterDiff?.phase2?.rre ?: entry.phase2Rre?.toKWh(),
      phase3Fae = electricityMeterDiff?.phase3?.fae ?: entry.phase3Fae?.toKWh(),
      phase3Rae = electricityMeterDiff?.phase3?.rae ?: entry.phase3Rae?.toKWh(),
      phase3Fre = electricityMeterDiff?.phase3?.fre ?: entry.phase3Fre?.toKWh(),
      phase3Rre = electricityMeterDiff?.phase3?.rre ?: entry.phase3Rre?.toKWh(),
      faeBalanced = electricityMeterDiff?.faeBalanced ?: entry.faeBalanced?.toKWh(),
      raeBalanced = electricityMeterDiff?.raeBalanced ?: entry.raeBalanced?.toKWh(),
      manuallyComplemented = manuallyComplemented,
      counterReset = electricityMeterDiff?.resetRecognized() ?: counterReset,
      profileId = profileId
    )
  }
}

data class ElectricityMeterDiff(
  val phase1: PhaseValues,
  val phase2: PhaseValues,
  val phase3: PhaseValues,
  var faeBalanced: Float? = null,
  var raeBalanced: Float? = null,
  override var reset: Boolean = false
) : SetWithResetDetection {
  override fun set(type: EnergyType, value: Float?) {
    when (type) {
      EnergyType.FAE_BALANCED -> faeBalanced = value
      EnergyType.RAE_BALANCED -> raeBalanced = value
      else -> throw IllegalStateException("Type `$type` is not applicable here!")
    }
  }

  fun resetRecognized() =
    phase1.reset || phase2.reset || phase3.reset || reset

  fun div(divider: Int): ElectricityMeterDiff =
    ElectricityMeterDiff(
      phase1 = phase1.div(divider),
      phase2 = phase2.div(divider),
      phase3 = phase3.div(divider),
      faeBalanced = faeBalanced?.div(divider),
      raeBalanced = raeBalanced?.div(divider),
      reset = reset
    )
}

data class PhaseValues(
  var fae: Float? = null,
  var rae: Float? = null,
  var fre: Float? = null,
  var rre: Float? = null,
  override var reset: Boolean = false
) : SetWithResetDetection {

  override fun set(type: EnergyType, value: Float?) {
    when (type) {
      EnergyType.FAE -> fae = value
      EnergyType.RAE -> rae = value
      EnergyType.FRE -> fre = value
      EnergyType.RRE -> rre = value
      else -> throw IllegalStateException("Type `$type` is not applicable here!")
    }
  }

  fun div(divider: Int): PhaseValues =
    PhaseValues(
      fae = fae?.div(divider),
      rae = rae?.div(divider),
      fre = fre?.div(divider),
      rre = rre?.div(divider),
      reset = reset
    )

  fun valueFor(chartType: ElectricityMeterChartType): Float? =
    when (chartType) {
      ElectricityMeterChartType.REVERSED_ACTIVE_ENERGY -> rae
      ElectricityMeterChartType.FORWARDED_ACTIVE_ENERGY -> fae
      ElectricityMeterChartType.REVERSED_REACTIVE_ENERGY -> rre
      ElectricityMeterChartType.FORWARDED_REACTIVE_ENERGY -> fre
      else -> null
    }

  fun valueFor(spec: ChartDataSpec): Float? =
    if (spec.customFilters is ElectricityChartFilters) {
      valueFor(spec.customFilters.type)
    } else {
      valueFor(ElectricityMeterChartType.FORWARDED_ACTIVE_ENERGY)
    }

  operator fun plus(other: PhaseValues): PhaseValues =
    PhaseValues(
      fae?.let { it + (other.fae ?: 0f) } ?: other.fae,
      rae?.let { it + (other.rae ?: 0f) } ?: other.rae,
      fre?.let { it + (other.fre ?: 0f) } ?: other.fre,
      rre?.let { it + (other.rre ?: 0f) } ?: other.rre,
      reset || other.reset
    )
}

private interface SetWithResetDetection {

  var reset: Boolean

  fun set(type: EnergyType, value: Float?)

  fun set(type: EnergyType, current: Float?, previous: Float?) {
    if (current == null) {
      return
    }
    if (previous == null) {
      set(type, current)
      return
    }

    val diff = current - previous
    if (diff < 0 && abs(diff) > previous.times(0.1)) {
      set(type, 0f)
      reset = true
    } else {
      set(type, max(0f, diff))
    }
  }
}

fun Long.toKWh(): Float = this.div(100_000.00f)
