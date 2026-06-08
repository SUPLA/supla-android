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
import org.supla.android.usecases.group.totalvalue.GroupValue
import org.supla.android.usecases.group.totalvalue.ShadingSystemGroupValue
import org.supla.core.shared.data.model.general.SuplaFunction

object ShadingSystemGroupActivePercentageProvider : GroupActivePercentageProvider {
  override fun handleFunction(function: SuplaFunction) = when (function) {
    SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
    SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
    SuplaFunction.TERRACE_AWNING,
    SuplaFunction.CURTAIN,
    SuplaFunction.ROLLER_GARAGE_DOOR -> true
    else -> false
  }

  override fun getActivePercentage(valueIndex: Int, values: List<GroupValue>) =
    values.map { (it as ShadingSystemGroupValue) }
      .fold(0) { acc, value -> if (value.position >= 100 || value.closeSensorActive) acc + 1 else acc }
      .times(100).div(values.count())
}
