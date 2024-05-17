package org.supla.android.usecases.group.activepercentage

import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.group.GroupActivePercentageProvider
import org.supla.android.usecases.group.totalvalue.DimmerAndRgbGroupValue
import org.supla.android.usecases.group.totalvalue.GroupValue

object DimmerAndRgbGroupActivePercentage : GroupActivePercentageProvider {
  override fun handleFunction(function: Int) =
    function == SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING

  override fun getActivePercentage(valueIndex: Int, values: List<GroupValue>) =
    values.map { (it as DimmerAndRgbGroupValue) }
      .fold(0) { acc, value ->
        var sum = acc
        if ((valueIndex == 0 || valueIndex == 1) && value.brightness > 0) {
          sum += 1
        }
        if ((valueIndex == 0 || valueIndex == 2) && value.brightnessColor > 0) {
          sum += 1
        }
        sum
      }
      .times(100).div(if (valueIndex == 0) values.count() * 2 else values.count())
}
