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
import androidx.recyclerview.widget.RecyclerView
import org.supla.android.ui.layouts.ChannelLayout
import org.supla.android.ui.lists.BaseListCallback

class ChannelsListCallback(
  context: Context,
  adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
) : BaseListCallback(context, adapter) {

  override fun canDropOver(
    recyclerView: RecyclerView,
    current: RecyclerView.ViewHolder,
    target: RecyclerView.ViewHolder
  ): Boolean {
    val currentView = current.itemView
    val targetView = target.itemView

    if (currentView !is ChannelLayout || targetView !is ChannelLayout) {
      return false
    }
    return currentView.locationId == targetView.locationId
  }
}