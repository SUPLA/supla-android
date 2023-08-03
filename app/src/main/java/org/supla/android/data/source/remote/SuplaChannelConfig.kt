package org.supla.android.data.source.remote
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

enum class ChannelConfigType(val value: Int) {
  DEFAULT(0),
  WEEKLY_SCHEDULE (2)
}

enum class ChannelConfigResult(val value: Int) {
  RESULT_FALSE(0),
  RESULT_TRUE(1),
  DATA_ERROR(2),
  TYPE_NOT_SUPPORTED(3),
  FUNCTION_NOT_SUPPORTED(4),
  LOCAL_CONFIG_DISABLED(5),
}

open class SuplaChannelConfig(remoteId: Int, func: Int) {
  open val remoteId: Int = remoteId
  open val func: Int = func
}