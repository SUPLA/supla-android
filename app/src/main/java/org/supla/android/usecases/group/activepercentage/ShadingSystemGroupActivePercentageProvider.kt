package org.supla.android.usecases.group.activepercentage

import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.group.GroupActivePercentageProvider
import org.supla.android.usecases.group.totalvalue.GroupValue
import org.supla.android.usecases.group.totalvalue.ShadingSystemGroupValue

object ShadingSystemGroupActivePercentageProvider : GroupActivePercentageProvider {
  override fun handleFunction(function: Int) = when (function) {
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW,
    SuplaConst.SUPLA_CHANNELFNC_TERRACE_AWNING,
    SuplaConst.SUPLA_CHANNELFNC_CURTAIN,
    SuplaConst.SUPLA_CHANNELFNC_ROLLER_GARAGE_DOOR -> true

    else -> false
  }

  override fun getActivePercentage(valueIndex: Int, values: List<GroupValue>) =
    values.map { (it as ShadingSystemGroupValue) }
      .fold(0) { acc, value -> if (value.position >= 100 || value.closeSensorActive) acc + 1 else acc }
      .times(100).div(values.count())
}
