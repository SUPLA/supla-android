package org.supla.android.usecases.group.activepercentage

import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.group.GroupActivePercentageProvider
import org.supla.android.usecases.group.totalvalue.GroupValue
import org.supla.android.usecases.group.totalvalue.HeatpolThermostatGroupValue

object HeatpolThermostatGroupActivePercentageProvider : GroupActivePercentageProvider {
  override fun handleFunction(function: Int) =
    function == SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS

  override fun getActivePercentage(valueIndex: Int, values: List<GroupValue>) =
    values.map { (it as HeatpolThermostatGroupValue).isOn }
      .fold(0) { acc, active -> if (active) acc + 1 else acc }
      .times(100).div(values.count())
}
