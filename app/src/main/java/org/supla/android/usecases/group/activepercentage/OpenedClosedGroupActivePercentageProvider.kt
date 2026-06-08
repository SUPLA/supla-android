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
import org.supla.android.usecases.group.totalvalue.OpenedClosedGroupValue
import org.supla.core.shared.data.model.general.SuplaFunction

object OpenedClosedGroupActivePercentageProvider : GroupActivePercentageProvider {
  override fun handleFunction(function: SuplaFunction) =
    when (function) {
      SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
      SuplaFunction.CONTROLLING_THE_GATE,
      SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
      SuplaFunction.POWER_SWITCH,
      SuplaFunction.LIGHTSWITCH,
      SuplaFunction.STAIRCASE_TIMER,
      SuplaFunction.VALVE_OPEN_CLOSE -> true
      else -> false
    }

  override fun getActivePercentage(valueIndex: Int, values: List<GroupValue>) =
    values.map { (it as OpenedClosedGroupValue).active }
      .fold(0) { acc, active -> if (active) acc + 1 else acc }
      .times(100).div(values.count())
}
