package org.supla.android.ui.lists
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
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.VISIBLE
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.databinding.ChannelListItemBinding
import org.supla.android.db.ChannelBase
import org.supla.android.db.Location
import org.supla.android.features.channellist.ChannelsListCallback
import org.supla.android.ui.layouts.ChannelLayout

abstract class BaseChannelsAdapter(
  private val context: Context,
  preferences: Preferences
) : BaseListAdapter<ListItem, ChannelBase>(context, preferences), ChannelLayout.Listener {

  var infoButtonClickCallback: (id: Int) -> Unit = { _ -> }
  var listItemClickCallback: (id: Int) -> Unit = { _ -> }

  override val callback = ChannelsListCallback(context, this).also {
    it.onMovedListener = { fromPos, toPos -> swapInternally(fromPos, toPos) }
    it.onMoveFinishedListener = {
      val channelsOrdered = items
        .filterIsInstance<ListItem.ChannelItem>()
        .map { sceneItem -> sceneItem.channelBase }

      if (movedItem != replacedItem) {
        swappedElementsCallback(
          (movedItem as? ListItem.ChannelItem)?.channelBase,
          (replacedItem as? ListItem.ChannelItem)?.channelBase
        )
      }
      movementFinishedCallback(channelsOrdered)
      cleanSwap()
    }
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      R.layout.channel_list_item -> {
        val binding = ChannelListItemBinding.inflate(inflater, parent, false)
        val holder = ChannelListItemViewHolder(binding)
        holder
      }
      else -> super.onCreateViewHolder(parent, viewType)
    }
  }

  override fun onBindViewHolder(vh: ViewHolder, pos: Int) {
    when (vh) {
      is ChannelListItemViewHolder -> {
        val channelBase = (items[pos] as ListItem.ChannelItem).channelBase
        vh.binding.channelLayout.setChannelData(channelBase)
        vh.binding.channelLayout.setChannelListener(this)
        vh.binding.channelLayout.setOnLongClickListener { onLongPress(vh) }
        vh.binding.channelLayout.setOnClickListener { listItemClickCallback(channelBase.remoteId) }
        vh.binding.channelLayout.setInfoIconClickListener { infoButtonClickCallback(channelBase.remoteId) }
      }
      is LocationListItemViewHolder -> {
        val location = (items[pos] as ListItem.LocationItem).location
        vh.binding.container.setOnClickListener {
          callback.closeWhenSwiped(withAnimation = false)
          toggleLocationCallback(location)
        }
        vh.binding.container.setOnLongClickListener { changeLocationCaption(location.locationId) }
        vh.binding.tvSectionCaption.text = location.caption
        vh.binding.ivSectionCollapsed.visibility = if (isLocationCollapsed(location)) {
          VISIBLE
        } else {
          GONE
        }
      }
    }
  }

  protected abstract fun isLocationCollapsed(location: Location): Boolean

  override fun getItemViewType(pos: Int): Int {
    return if (items[pos] is ListItem.ChannelItem) {
      R.layout.channel_list_item
    } else {
      R.layout.location_list_item
    }
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  private fun onLongPress(viewHolder: ViewHolder): Boolean {
    SuplaApp.Vibrate(context)
    callback.closeWhenSwiped()
    itemTouchHelper.startDrag(viewHolder)

    return true
  }

  inner class ChannelListItemViewHolder(val binding: ChannelListItemBinding) :
    ViewHolder(binding.root)
}
