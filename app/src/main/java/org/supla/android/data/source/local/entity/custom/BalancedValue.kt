package org.supla.android.data.source.local.entity.custom

import androidx.room.ColumnInfo
import org.supla.android.data.source.local.entity.measurements.BaseLogEntity
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import java.util.Date

data class BalancedValue(
  @ColumnInfo(name = ElectricityMeterLogEntity.COLUMN_TIMESTAMP) override val date: Date,
  @ColumnInfo(name = ElectricityMeterLogEntity.COLUMN_GROUPING_STRING) override val groupingString: String,
  @ColumnInfo(name = "consumption") val forwarded: Float,
  @ColumnInfo(name = "production") val reversed: Float
) : BaseLogEntity
