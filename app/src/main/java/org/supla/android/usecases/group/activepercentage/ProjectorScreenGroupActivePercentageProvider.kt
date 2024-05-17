package org.supla.android.usecases.group.activepercentage

import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.group.GroupActivePercentageProvider
import org.supla.android.usecases.group.totalvalue.GroupValue
import org.supla.android.usecases.group.totalvalue.ProjectorScreenGroupValue

object ProjectorScreenGroupActivePercentageProvider : GroupActivePercentageProvider {
  override fun handleFunction(function: Int) =
    function == SuplaConst.SUPLA_CHANNELFNC_PROJECTOR_SCREEN

  override fun getActivePercentage(valueIndex: Int, values: List<GroupValue>) =
    values.map { (it as ProjectorScreenGroupValue).position }
      .fold(0) { acc, position -> if (position >= 100) acc + 1 else acc }
      .times(100).div(values.count())
}
