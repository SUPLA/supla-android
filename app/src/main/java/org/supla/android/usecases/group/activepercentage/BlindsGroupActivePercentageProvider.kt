package org.supla.android.usecases.group.activepercentage

import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_VERTICAL_BLIND
import org.supla.android.usecases.group.GroupActivePercentageProvider
import org.supla.android.usecases.group.totalvalue.GroupValue
import org.supla.android.usecases.group.totalvalue.ShadowingBlindGroupValue

object BlindsGroupActivePercentageProvider : GroupActivePercentageProvider {
  override fun handleFunction(function: Int) =
    when (function) {
      SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND,
      SUPLA_CHANNELFNC_VERTICAL_BLIND -> true

      else -> false
    }

  override fun getActivePercentage(valueIndex: Int, values: List<GroupValue>) =
    values.map { (it as ShadowingBlindGroupValue) }
      .fold(0) { acc, value -> if (value.position >= 100) acc + 1 else acc }
      .times(100).div(values.count())
}
