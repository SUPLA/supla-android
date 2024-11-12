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

import org.supla.core.shared.data.model.battery.BatteryInfo

sealed interface BaseData {
  val remoteId: Int
  val caption: String
}

data class Channel(
  override val remoteId: Int,
  override val caption: String,
  val online: Boolean,
  val function: SuplaFunction,
  val batteryInfo: BatteryInfo?,
  val value: ByteArray?
) : BaseData

data class Group(
  override val remoteId: Int,
  override val caption: String,
  val function: SuplaFunction
) : BaseData

data class Scene(
  override val remoteId: Int,
  override val caption: String
) : BaseData
