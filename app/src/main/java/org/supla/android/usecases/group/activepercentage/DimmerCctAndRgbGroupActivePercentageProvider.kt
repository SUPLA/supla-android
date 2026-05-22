package org.supla.android.usecases.group.activepercentage
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import org.supla.android.usecases.group.GroupActivePercentageProvider
import org.supla.android.usecases.group.totalvalue.DimmerCctAndRgbGroupValue
import org.supla.android.usecases.group.totalvalue.GroupValue
import org.supla.core.shared.data.model.general.SuplaFunction

object DimmerCctAndRgbGroupActivePercentageProvider : GroupActivePercentageProvider {

  override fun handleFunction(function: SuplaFunction) =
    function == SuplaFunction.DIMMER_CCT_AND_RGB

  override fun getActivePercentage(valueIndex: Int, values: List<GroupValue>) =
    values.map { (it as DimmerCctAndRgbGroupValue) }
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
