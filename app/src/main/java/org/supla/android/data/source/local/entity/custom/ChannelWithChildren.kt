package org.supla.android.data.source.local.entity.custom
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
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.isElectricityMeter
import org.supla.android.data.source.local.entity.complex.onlineState
import org.supla.android.data.source.local.entity.isElectricityMeter
import org.supla.android.data.source.local.entity.isImpulseCounter
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.lib.SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS
import org.supla.android.lib.SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.lists.onlineState
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.general.SuplaFunction

data class ChannelWithChildren(
  val channel: ChannelDataEntity,
  val children: List<ChannelChildEntity> = emptyList()
) : ChannelDataBase {

  override val locationCaption: String
    get() = channel.locationCaption

  override val id: Long?
    get() = channel.id
  override val remoteId: Int
    get() = channel.remoteId
  override val function: SuplaFunction
    get() = channel.function
  override val caption: String
    get() = channel.caption
  override val locationId: Int
    get() = channel.locationId
  override val flags: Long
    get() = channel.flags
  override val visible: Int
    get() = channel.visible
  override val userIcon: Int
    get() = channel.userIcon
  override val altIcon: Int
    get() = channel.altIcon
  override val profileId: Long
    get() = channel.profileId
  override val status: SuplaChannelAvailabilityStatus
    get() = channel.status

  val allDescendantFlat: List<ChannelChildEntity>
    get() = getChildren(children)

  val pumpSwitchChild: ChannelChildEntity?
    get() = children.firstOrNull { it.relationType == ChannelRelationType.PUMP_SWITCH }

  val heatOrColdSourceSwitchChild: ChannelChildEntity?
    get() = children.firstOrNull { it.relationType == ChannelRelationType.HEAT_OR_COLD_SOURCE_SWITCH }

  val isOrHasImpulseCounter: Boolean
    get() = channel.isImpulseCounter() || channel.channelValueEntity.subValueType == SUBV_TYPE_IC_MEASUREMENTS.toShort() ||
      children.firstOrNull { it.relationType == ChannelRelationType.METER }?.channel?.isImpulseCounter() == true

  val isOrHasElectricityMeter: Boolean
    get() = channel.isElectricityMeter() || channel.channelValueEntity.subValueType == SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort() ||
      children.firstOrNull { it.relationType == ChannelRelationType.METER }?.channel?.isElectricityMeter() == true

  val onlineState: ListOnlineState
    get() = channel.channelValueEntity.status.onlineState mergeWith children.onlineState

  override fun onlinePercentage(): Int = channel.onlinePercentage()

  private fun getChildren(tree: List<ChannelChildEntity>): List<ChannelChildEntity> =
    tree.flatMap { if (it.children.isEmpty()) mutableListOf(it) else getChildren(it.children).add(it) }

  private fun List<ChannelChildEntity>.add(item: ChannelChildEntity): List<ChannelChildEntity> {
    (this as? MutableList<ChannelChildEntity>)?.add(item)
    return this
  }
}
