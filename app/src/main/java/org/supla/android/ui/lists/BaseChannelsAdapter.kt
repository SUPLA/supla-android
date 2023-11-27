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
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.remote.thermostat.SuplaThermostatFlags
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.databinding.LiChannelItemBinding
import org.supla.android.databinding.LiThermostatItemBinding
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.isHvacThermostat
import org.supla.android.extensions.toThermostatSlideableListItemData
import org.supla.android.ui.layouts.ChannelLayout
import org.supla.android.ui.lists.data.SlideableListItemData

abstract class BaseChannelsAdapter(
  private val context: Context,
  private val valuesFormatter: ValuesFormatter,
  preferences: Preferences
) : BaseListAdapter<ListItem, ChannelBase>(context, preferences), ChannelLayout.Listener {

  var infoButtonClickCallback: (id: Int) -> Unit = { _ -> }
  var issueButtonClickCallback: (messageId: Int?) -> Unit = { _ -> }
  var listItemClickCallback: (channelBase: ChannelBase) -> Unit = { _ -> }

  override val callback = ListCallback(context, this).also {
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
      R.layout.li_channel_item ->
        ChannelListItemViewHolder(LiChannelItemBinding.inflate(inflater, parent, false))

      R.layout.li_thermostat_item ->
        ThermostatListItemViewHolder(LiThermostatItemBinding.inflate(inflater, parent, false))

      else -> super.onCreateViewHolder(parent, viewType)
    }
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    when (holder) {
      is ChannelListItemViewHolder -> holder.bind(items[position] as ListItem.ChannelItem)
      is ThermostatListItemViewHolder -> holder.bind(items[position] as ListItem.ChannelItem)
      else -> super.onBindViewHolder(holder, position)
    }
  }

  override fun getItemViewType(pos: Int): Int {
    return when (val item = items[pos]) {
      is ListItem.ChannelItem -> {
        if (item.channelBase.isHvacThermostat()) {
          R.layout.li_thermostat_item
        } else {
          R.layout.li_channel_item
        }
      }

      else -> R.layout.li_location_item
    }
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun onItemClick(channelBase: ChannelBase) {
    listItemClickCallback(channelBase)
  }

  private fun onLongPress(viewHolder: ViewHolder): Boolean {
    SuplaApp.Vibrate(context)
    callback.closeWhenSwiped()
    itemTouchHelper.startDrag(viewHolder)

    return true
  }

  inner class ChannelListItemViewHolder(val binding: LiChannelItemBinding) : ViewHolder(binding.root) {
    fun bind(item: ListItem.ChannelItem) {
      binding.channelLayout.setChannelData(item.channelBase)
      binding.channelLayout.setLocationCaption(item.location.caption)
      binding.channelLayout.setChannelListener(this@BaseChannelsAdapter)
      binding.channelLayout.setOnLongClickListener { onLongPress(this) }
      binding.channelLayout.setOnClickListener { listItemClickCallback(binding.channelLayout.channelBase) }
      binding.channelLayout.setInfoIconClickListener { infoButtonClickCallback(item.channelBase.remoteId) }
    }
  }

  inner class ThermostatListItemViewHolder(val binding: LiThermostatItemBinding) : ViewHolder(binding.root) {
    fun bind(item: ListItem.ChannelItem) {
      binding.listItemRoot.bind(
        itemType = ItemType.CHANNEL,
        remoteId = item.channelBase.remoteId,
        locationCaption = item.location.caption,
        data = data(item),
        onInfoClick = { infoButtonClickCallback(item.channelBase.remoteId) },
        onIssueClick = { issueButtonClickCallback(getIssueMessage(item)) },
        onTitleLongClick = { onCaptionLongPress(item.channelBase.remoteId) },
        onItemClick = { listItemClickCallback(item.channelBase) }
      )

      binding.listItemContent.setOnClickListener { listItemClickCallback(item.channelBase) }
      binding.listItemContent.setOnLongClickListener { onLongPress(this) }
      binding.listItemLeftItem.setOnClickListener { onLeftButtonClick(item.channelBase.remoteId) }
      binding.listItemRightItem.setOnClickListener { onRightButtonClick(item.channelBase.remoteId) }
    }

    private fun data(item: ListItem.ChannelItem): SlideableListItemData.Thermostat {
      val (channel) = guardLet(item.channelBase as? Channel) {
        throw IllegalArgumentException("Expected Channel but got ${item.channelBase}")
      }
      val child = item.children?.firstOrNull { it.relationType == ChannelRelationType.MAIN_THERMOMETER }

      return channel.toThermostatSlideableListItemData(child?.channel, valuesFormatter)
    }

    private fun getIssueMessage(item: ListItem.ChannelItem): Int? {
      val (channel) = guardLet(item.channelBase as? Channel) { return null }
      val value = channel.value.asThermostatValue()

      return if (value.flags.contains(SuplaThermostatFlags.THERMOMETER_ERROR)) {
        R.string.thermostat_thermometer_error
      } else if (value.flags.contains(SuplaThermostatFlags.CLOCK_ERROR)) {
        R.string.thermostat_clock_error
      } else {
        null
      }
    }
  }
}
