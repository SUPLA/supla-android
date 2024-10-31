package org.supla.android.core.shared.data.model.lists
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

import androidx.annotation.DimenRes
import org.supla.android.R
import org.supla.core.shared.data.model.lists.IssueIcon

val IssueIcon.resource: Int
  get() = when (this) {
    IssueIcon.Warning -> R.drawable.channel_warning_level1
    IssueIcon.Error -> R.drawable.channel_warning_level2
    IssueIcon.Battery -> R.drawable.battery
    IssueIcon.Battery0 -> R.drawable.battery_0
    IssueIcon.Battery25 -> R.drawable.battery_25
    IssueIcon.Battery50 -> R.drawable.battery_50
    IssueIcon.Battery75 -> R.drawable.battery_75
    IssueIcon.Battery100 -> R.drawable.battery_100
    IssueIcon.BatteryNotUsed -> R.drawable.battery_not_used
  }

val IssueIcon.width: Int
  @DimenRes
  get() = R.dimen.icon_default_size

val IssueIcon.height: Int
  @DimenRes
  get() = R.dimen.icon_default_size

val IssueIcon.rotation: Float
  get() = 0f
