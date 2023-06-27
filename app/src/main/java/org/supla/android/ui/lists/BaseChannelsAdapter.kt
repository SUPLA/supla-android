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
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.databinding.LiChannelItemBinding
import org.supla.android.db.ChannelBase
import org.supla.android.features.channellist.ChannelsListCallback
import org.supla.android.ui.layouts.ChannelLayout

abstract class BaseChannelsAdapter(
  private val context: Context,
  preferences: Preferences
) : BaseListAdapter<ListItem, ChannelBase>(context, preferences), ChannelLayout.Listener {

  var infoButtonClickCallback: (id: Int) -> Unit = { _ -> }
  var listItemClickCallback: (channelBase: ChannelBase) -> Unit = { _ -> }

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
      R.layout.li_channel_item -> {
        val binding = LiChannelItemBinding.inflate(inflater, parent, false)
        val holder = ChannelListItemViewHolder(binding)
        holder
      }
      else -> super.onCreateViewHolder(parent, viewType)
    }
  }

  override fun onBindViewHolder(vh: ViewHolder, pos: Int) {
    when (vh) {
      is ChannelListItemViewHolder -> {
        val item = (items[pos] as ListItem.ChannelItem)
        vh.binding.channelLayout.setChannelData(item.channelBase)
        vh.binding.channelLayout.setLocationCaption(item.location.caption)
        vh.binding.channelLayout.setChannelListener(this)
        vh.binding.channelLayout.setOnLongClickListener { onLongPress(vh) }
        vh.binding.channelLayout.setOnClickListener { listItemClickCallback(vh.binding.channelLayout.channelBase) }
        vh.binding.channelLayout.setInfoIconClickListener { infoButtonClickCallback(item.channelBase.remoteId) }
      }
      else -> super.onBindViewHolder(vh, pos)
    }
  }

  override fun getItemViewType(pos: Int): Int {
    return if (items[pos] is ListItem.ChannelItem) {
      R.layout.li_channel_item
    } else {
      R.layout.li_location_item
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

  inner class ChannelListItemViewHolder(val binding: LiChannelItemBinding) :
    ViewHolder(binding.root)
}
