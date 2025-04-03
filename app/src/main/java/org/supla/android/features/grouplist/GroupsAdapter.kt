package org.supla.android.features.grouplist
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

import android.content.Context
import dagger.hilt.android.qualifiers.ActivityContext
import org.supla.android.Preferences
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.tools.VibrationHelper
import org.supla.android.ui.lists.BaseChannelsAdapter
import javax.inject.Inject

class GroupsAdapter @Inject constructor(
  @ActivityContext private val context: Context,
  vibrationHelper: VibrationHelper,
  preferences: Preferences
) : BaseChannelsAdapter(vibrationHelper, context, preferences) {

  override fun isLocationCollapsed(location: LocationEntity) = ((location.collapsed and 0x2) > 0)
}
