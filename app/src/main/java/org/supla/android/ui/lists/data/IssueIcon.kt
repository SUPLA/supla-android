package org.supla.android.ui.lists.data
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
import androidx.annotation.DrawableRes
import org.supla.android.R

sealed class IssueIcon(
  @DrawableRes val resource: Int,
  @DimenRes val width: Int,
  @DimenRes val height: Int,
  val rotation: Float = 0f
) {
  data object Warning : IssueIcon(R.drawable.channel_warning_level1, R.dimen.icon_default_size, R.dimen.icon_default_size)
  data object Error : IssueIcon(R.drawable.channel_warning_level2, R.dimen.icon_default_size, R.dimen.icon_default_size)
  data object Battery : IssueIcon(R.drawable.battery, R.dimen.icon_default_size, R.dimen.icon_default_size)
  data object Battery0 : IssueIcon(R.drawable.battery_0, R.dimen.icon_default_size, R.dimen.icon_default_size)
  data object Battery25 : IssueIcon(R.drawable.battery_25, R.dimen.icon_default_size, R.dimen.icon_default_size)
  data object Battery50 : IssueIcon(R.drawable.battery_50, R.dimen.icon_default_size, R.dimen.icon_default_size)
  data object Battery75 : IssueIcon(R.drawable.battery_75, R.dimen.icon_default_size, R.dimen.icon_default_size)
  data object Battery100 : IssueIcon(R.drawable.battery_100, R.dimen.icon_default_size, R.dimen.icon_default_size)
  data object BatteryNotUsed : IssueIcon(R.drawable.battery_not_used, R.dimen.icon_default_size, R.dimen.icon_default_size)
}
