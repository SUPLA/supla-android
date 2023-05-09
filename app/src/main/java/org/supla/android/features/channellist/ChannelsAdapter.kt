package org.supla.android.features.channellist
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
import org.supla.android.*
import org.supla.android.db.Location
import org.supla.android.ui.layouts.ChannelLayout
import org.supla.android.ui.lists.BaseChannelsAdapter
import javax.inject.Inject

class ChannelsAdapter @Inject constructor(
  @ActivityContext private val context: Context,
  preferences: Preferences
) : BaseChannelsAdapter(context, preferences), ChannelLayout.Listener {

  override fun isLocationCollapsed(location: Location) = ((location.collapsed and 0x1) > 0)

  override fun onCaptionLongPress(channelId: Int) {
    SuplaApp.Vibrate(context)
//    val editor = ChannelCaptionEditor(context)
//    editor.captionChangedListener = reloadCallback
//    editor.edit(channelId)
  }
}
