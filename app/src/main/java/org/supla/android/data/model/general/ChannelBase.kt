package org.supla.android.data.model.general
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

import org.supla.android.usecases.channel.GetChannelStateUseCase
import org.supla.core.shared.data.model.general.SuplaFunction

interface ChannelBase {
  val id: Long?
  val remoteId: Int
  val function: SuplaFunction
  val caption: String
  val locationId: Int
  val flags: Long
  val visible: Int
  val userIcon: Int
  val altIcon: Int
  val profileId: Long

  val offlineState: ChannelState
    get() = GetChannelStateUseCase.getOfflineState(function)
}
