package org.supla.android.lib

import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.tools.UsedFromNativeCode

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

open class SuplaChannelBase @UsedFromNativeCode constructor() {
  @JvmField
  var EOL: Boolean = false

  @JvmField
  var Id: Int = 0

  @JvmField
  var LocationID: Int = 0

  @JvmField
  var Func: Int = 0

  @JvmField
  var AltIcon: Int = 0

  @JvmField
  var UserIcon: Int = 0

  @JvmField
  var Flags: Long = 0
  var AvailabilityStatus: SuplaChannelAvailabilityStatus? = null

  @JvmField
  var Caption: String = ""

  val OnLine: Boolean
    get() = AvailabilityStatus == SuplaChannelAvailabilityStatus.ONLINE
}
