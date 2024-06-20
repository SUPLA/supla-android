package org.supla.android.usecases.group
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

import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.db.ChannelGroup
import org.supla.android.usecases.group.activepercentage.BlindsGroupActivePercentageProvider
import org.supla.android.usecases.group.activepercentage.DimmerAndRgbGroupActivePercentage
import org.supla.android.usecases.group.activepercentage.DimmerGroupActivePercentageProvider
import org.supla.android.usecases.group.activepercentage.HeatpolThermostatGroupActivePercentageProvider
import org.supla.android.usecases.group.activepercentage.OpenedClosedGroupActivePercentageProvider
import org.supla.android.usecases.group.activepercentage.ProjectorScreenGroupActivePercentageProvider
import org.supla.android.usecases.group.activepercentage.RgbGroupActivePercentageProvider
import org.supla.android.usecases.group.activepercentage.ShadingSystemGroupActivePercentageProvider
import org.supla.android.usecases.group.totalvalue.GroupTotalValue.Companion.parse
import org.supla.android.usecases.group.totalvalue.GroupValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetGroupActivePercentageUseCase @Inject constructor() {

  private val providers: List<GroupActivePercentageProvider> = listOf(
    OpenedClosedGroupActivePercentageProvider,
    ShadingSystemGroupActivePercentageProvider,
    BlindsGroupActivePercentageProvider,
    ProjectorScreenGroupActivePercentageProvider,
    DimmerGroupActivePercentageProvider,
    RgbGroupActivePercentageProvider,
    DimmerAndRgbGroupActivePercentage,
    HeatpolThermostatGroupActivePercentageProvider
  )

  operator fun invoke(channelGroupEntity: ChannelGroupEntity, valueIndex: Int = 0): Int =
    getActivePercentage(channelGroupEntity.function, channelGroupEntity.totalValue, valueIndex)

  operator fun invoke(channelGroup: ChannelGroup, valueIndex: Int = 0) =
    getActivePercentage(channelGroup.func, channelGroup.totalValue, valueIndex)

  private fun getActivePercentage(function: Int, totalValue: String?, valueIndex: Int): Int {
    val values = parse(function, totalValue)
    if (values.isEmpty()) {
      return 0
    }

    providers.forEach {
      if (it.handleFunction(function)) {
        return it.getActivePercentage(valueIndex, values)
      }
    }

    return 0
  }
}

interface GroupActivePercentageProvider {
  fun handleFunction(function: Int): Boolean

  fun getActivePercentage(valueIndex: Int, values: List<GroupValue>): Int
}
