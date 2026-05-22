package org.supla.android.usecases.group.activepercentage

import org.supla.android.usecases.group.GroupActivePercentageProvider
import org.supla.android.usecases.group.totalvalue.DimmerGroupValue
import org.supla.android.usecases.group.totalvalue.GroupValue
import org.supla.core.shared.data.model.general.SuplaFunction

object DimmerGroupActivePercentageProvider : GroupActivePercentageProvider {
  override fun handleFunction(function: SuplaFunction) =
    function == SuplaFunction.DIMMER

  override fun getActivePercentage(valueIndex: Int, values: List<GroupValue>) =
    values.map { (it as DimmerGroupValue).brightness }
      .fold(0) { acc, brightness -> if (brightness > 0) acc + 1 else acc }
      .times(100).div(values.count())
}
