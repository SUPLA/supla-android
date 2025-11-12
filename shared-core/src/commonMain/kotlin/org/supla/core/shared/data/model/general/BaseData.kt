package org.supla.core.shared.data.model.general
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

import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.core.shared.data.model.battery.BatteryInfo
import org.supla.core.shared.data.model.channel.ChannelState

sealed interface BaseData {
  val remoteId: Int
  val caption: String
}

data class Channel(
  override val remoteId: Int,
  override val caption: String,
  val status: SuplaChannelAvailabilityStatus,
  val function: SuplaFunction,
  val altIcon: Int,
  val batteryInfo: BatteryInfo?,
  val channelState: ChannelState?,
  val value: ByteArray?
) : BaseData {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as Channel

    if (remoteId != other.remoteId) return false
    if (altIcon != other.altIcon) return false
    if (caption != other.caption) return false
    if (status != other.status) return false
    if (function != other.function) return false
    if (batteryInfo != other.batteryInfo) return false
    if (channelState != other.channelState) return false
    if (!value.contentEquals(other.value)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = remoteId
    result = 31 * result + altIcon
    result = 31 * result + caption.hashCode()
    result = 31 * result + status.hashCode()
    result = 31 * result + function.hashCode()
    result = 31 * result + (batteryInfo?.hashCode() ?: 0)
    result = 31 * result + (channelState?.hashCode() ?: 0)
    result = 31 * result + (value?.contentHashCode() ?: 0)
    return result
  }
}

data class Group(
  override val remoteId: Int,
  override val caption: String,
  val function: SuplaFunction
) : BaseData

data class Scene(
  override val remoteId: Int,
  override val caption: String
) : BaseData
