package org.supla.android.features.scenelist
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
import org.supla.android.ui.layouts.SceneLayout
import org.supla.android.ui.lists.BaseListCallback

class SceneListCallback(
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

    if (currentView !is SceneLayout || targetView !is SceneLayout) {
      return false
    }
    return currentView.locationId == targetView.locationId
  }
}