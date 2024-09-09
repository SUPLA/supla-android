package org.supla.android.usecases.details
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

import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.local.entity.isElectricityMeter
import org.supla.android.data.source.local.entity.isImpulseCounter
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.features.details.detailbase.standarddetail.DetailPage
import org.supla.android.lib.SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS
import org.supla.android.lib.SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER
import org.supla.android.usecases.channel.ChannelWithChildren
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProvideChannelDetailTypeUseCase @Inject constructor() : BaseDetailTypeProviderUseCase() {

  operator fun invoke(channelWithChildren: ChannelWithChildren): DetailType? = when (val function = channelWithChildren.channel.function) {
    SUPLA_CHANNELFNC_LIGHTSWITCH,
    SUPLA_CHANNELFNC_POWERSWITCH,
    SUPLA_CHANNELFNC_STAIRCASETIMER -> SwitchDetailType(getSwitchDetailPages(channelWithChildren))

    else -> provide(function)
  }

  private fun getSwitchDetailPages(channelWithChildren: ChannelWithChildren): List<DetailPage> {
    val list = mutableListOf(DetailPage.SWITCH)
    if (supportsTimer(channelWithChildren.channel)) {
      list.add(DetailPage.SWITCH_TIMER)
    }

    val meterChild = channelWithChildren.children.firstOrNull { it.relationType == ChannelRelationType.METER }
    if (meterChild?.channel?.isElectricityMeter() == true) {
      list.add(DetailPage.EM_HISTORY)
      list.add(DetailPage.EM_SETTINGS)
    } else if (meterChild?.channel?.isImpulseCounter() == true) {
      list.add(DetailPage.HISTORY_IC)
    } else if (channelWithChildren.channel.channelValueEntity.subValueType == SUBV_TYPE_IC_MEASUREMENTS.toShort()) {
      list.add(DetailPage.HISTORY_IC)
    } else if (channelWithChildren.channel.channelValueEntity.subValueType == SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort()) {
      list.add(DetailPage.EM_HISTORY)
      list.add(DetailPage.EM_SETTINGS)
    }

    return list
  }

  private fun supportsTimer(channelDataBase: ChannelDataBase) =
    SuplaChannelFlag.COUNTDOWN_TIMER_SUPPORTED inside channelDataBase.flags &&
      channelDataBase.function != SUPLA_CHANNELFNC_STAIRCASETIMER
}
