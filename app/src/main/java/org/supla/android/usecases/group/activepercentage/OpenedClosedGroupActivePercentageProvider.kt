package org.supla.android.usecases.group.activepercentage

import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.group.GroupActivePercentageProvider
import org.supla.android.usecases.group.totalvalue.GroupValue
import org.supla.android.usecases.group.totalvalue.OpenedClosedGroupValue

object OpenedClosedGroupActivePercentageProvider : GroupActivePercentageProvider {
  override fun handleFunction(function: Int) =
    when (function) {
      SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK,
      SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK,
      SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE,
      SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR,
      SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH,
      SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER,
      SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE -> true

      else -> false
    }

  override fun getActivePercentage(valueIndex: Int, values: List<GroupValue>) =
    values.map { (it as OpenedClosedGroupValue).active }
      .fold(0) { acc, active -> if (active) acc + 1 else acc }
      .times(100).div(values.count())
}
