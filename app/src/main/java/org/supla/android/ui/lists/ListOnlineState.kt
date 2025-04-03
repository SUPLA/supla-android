package org.supla.android.ui.lists
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
import org.supla.android.ui.lists.ListOnlineState.OFFLINE
import org.supla.android.ui.lists.ListOnlineState.ONLINE
import org.supla.android.ui.lists.ListOnlineState.UPDATING

enum class ListOnlineState {
  ONLINE, PARTIALLY_ONLINE, OFFLINE, UPDATING, UNKNOWN;

  val online: Boolean
    get() = this == ONLINE || this == PARTIALLY_ONLINE

  infix fun mergeWith(other: ListOnlineState?): ListOnlineState = merge(this, other)
}

val SuplaChannelAvailabilityStatus.onlineState: ListOnlineState
  get() = when (this) {
    SuplaChannelAvailabilityStatus.ONLINE -> ONLINE
    SuplaChannelAvailabilityStatus.FIRMWARE_UPDATE_ONGOING -> UPDATING
    else -> OFFLINE
  }

private val ListOnlineState?.onlineForMerge: Boolean
  get() = this == ONLINE

private val ListOnlineState?.offlineForMerge: Boolean
  get() = this == OFFLINE || this == UPDATING

fun merge(first: ListOnlineState, second: ListOnlineState?): ListOnlineState =
  if (first == second) {
    first
  } else if (first == ListOnlineState.PARTIALLY_ONLINE || second == ListOnlineState.PARTIALLY_ONLINE) {
    ListOnlineState.PARTIALLY_ONLINE
  } else if (first.onlineForMerge && second.offlineForMerge) {
    ListOnlineState.PARTIALLY_ONLINE
  } else if (first.offlineForMerge && second.onlineForMerge) {
    ListOnlineState.PARTIALLY_ONLINE
  } else if (first.onlineForMerge || second.onlineForMerge) {
    ONLINE
  } else {
    OFFLINE
  }
