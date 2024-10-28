package org.supla.android.usecases.channel
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

import org.supla.android.data.model.battery.batterInfo
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.ui.lists.data.IssueIcon
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChannelBatteryIconUseCase @Inject constructor() {

  operator fun invoke(channelWithChildren: ChannelWithChildren): IssueIcon? {
    val batteryInfo = channelWithChildren.channel.batterInfo

    return when {
      batteryInfo?.batteryPowered == false -> IssueIcon.BatteryNotUsed
      batteryInfo?.level?.let { it > 75 } ?: false -> IssueIcon.Battery100
      batteryInfo?.level?.let { it > 50 } ?: false -> IssueIcon.Battery75
      batteryInfo?.level?.let { it > 25 } ?: false -> IssueIcon.Battery50
      batteryInfo?.level?.let { it > 10 } ?: false -> IssueIcon.Battery25
      batteryInfo?.level?.let { it > 0 } ?: false -> IssueIcon.Battery0
      batteryInfo?.batteryPowered == true -> IssueIcon.Battery
      else -> null
    }
  }
}
