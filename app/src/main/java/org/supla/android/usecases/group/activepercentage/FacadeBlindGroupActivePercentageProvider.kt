package org.supla.android.usecases.group.activepercentage

import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
import org.supla.android.usecases.group.GroupActivePercentageProvider
import org.supla.android.usecases.group.totalvalue.GroupValue
import org.supla.android.usecases.group.totalvalue.ShadowingBlindGroupValue

object FacadeBlindGroupActivePercentageProvider : GroupActivePercentageProvider {
  override fun handleFunction(function: Int) =
    function == SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND

  override fun getActivePercentage(valueIndex: Int, values: List<GroupValue>) =
    values.map { (it as ShadowingBlindGroupValue) }
      .fold(0) { acc, value -> if (value.position >= 100) acc + 1 else acc }
      .times(100).div(values.count())
}
