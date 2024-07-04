package org.supla.android.usecases.group.activepercentage

import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.group.GroupActivePercentageProvider
import org.supla.android.usecases.group.totalvalue.GroupValue
import org.supla.android.usecases.group.totalvalue.RgbGroupValue

object RgbGroupActivePercentageProvider : GroupActivePercentageProvider {
  override fun handleFunction(function: Int) =
    function == SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING

  override fun getActivePercentage(valueIndex: Int, values: List<GroupValue>) =
    values.map { (it as RgbGroupValue).brightness }
      .fold(0) { acc, brightness -> if (brightness > 0) acc + 1 else acc }
      .times(100).div(values.count())
}
