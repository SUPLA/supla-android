package org.supla.android.usecases.group.activepercentage

import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMER
import org.supla.android.usecases.group.GroupActivePercentageProvider
import org.supla.android.usecases.group.totalvalue.DimmerGroupValue
import org.supla.android.usecases.group.totalvalue.GroupValue

object DimmerGroupActivePercentageProvider : GroupActivePercentageProvider {
  override fun handleFunction(function: Int) =
    function == SUPLA_CHANNELFNC_DIMMER

  override fun getActivePercentage(valueIndex: Int, values: List<GroupValue>) =
    values.map { (it as DimmerGroupValue).brightness }
      .fold(0) { acc, brightness -> if (brightness > 0) acc + 1 else acc }
      .times(100).div(values.count())
}
