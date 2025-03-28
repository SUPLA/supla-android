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
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.databinding.LiChannelItemBinding
import org.supla.android.databinding.LiMainDoubleValueItemBinding
import org.supla.android.databinding.LiMainIconValueItemBinding
import org.supla.android.databinding.LiMainIconValueWithButtonsItemBinding
import org.supla.android.databinding.LiMainIconValueWithRightButtonItemBinding
import org.supla.android.databinding.LiMainThermostatItemBinding
import org.supla.android.tools.VibrationHelper
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.core.shared.data.model.lists.ListItemIssues

abstract class BaseChannelsAdapter(
  private val vibrationHelper: VibrationHelper,
  private val context: Context,
  preferences: Preferences
) : BaseListAdapter<ChannelDataBase>(preferences) {

  var infoButtonClickCallback: (id: Int) -> Unit = { _ -> }
  var issueButtonClickCallback: (issues: ListItemIssues) -> Unit = { _ -> }
  var listItemClickCallback: (remoteId: Int) -> Unit = { _ -> }

  override val callback = ListCallback(context, this).also {
    it.onMovedListener = { fromPos, toPos -> swapInternally(fromPos, toPos) }
    it.onMoveFinishedListener = {
      val channelsOrdered = items
        .filterIsInstance<ListItem.ChannelItem>()
        .map { item -> item.channelBase }

      if (movedItem != replacedItem) {
        swappedElementsCallback(
          (movedItem as? ListItem.ChannelBasedItem)?.channelBase,
          (replacedItem as? ListItem.ChannelBasedItem)?.channelBase
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
      ViewType.CHANNEL_ITEM.ordinal ->
        ChannelListItemViewHolder(LiChannelItemBinding.inflate(inflater, parent, false))

      ViewType.HVAC_ITEM.ordinal ->
        ThermostatListItemViewHolder(LiMainThermostatItemBinding.inflate(inflater, parent, false))

      ViewType.ICON_VALUE_ITEM.ordinal ->
        IconValueListItemViewHolder(LiMainIconValueItemBinding.inflate(inflater, parent, false))

      ViewType.ICON_WITH_BUTTONS_ITEM.ordinal ->
        IconWithButtonsItemViewHolder(LiMainIconValueWithButtonsItemBinding.inflate(inflater, parent, false))

      ViewType.DOUBLE_VALUE_ITEM.ordinal ->
        DoubleValueListItemViewHolder(LiMainDoubleValueItemBinding.inflate(inflater, parent, false))

      ViewType.ICON_WITH_RIGHT_BUTTON_ITEM.ordinal ->
        IconWithRightButtonItemViewHolder(LiMainIconValueWithRightButtonItemBinding.inflate(inflater, parent, false))

      else -> super.onCreateViewHolder(parent, viewType)
    }
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val item = items[position]
    when (holder) {
      is ChannelListItemViewHolder -> holder.bind(item as ListItem.ChannelItem)
      is ThermostatListItemViewHolder -> holder.bind(item as ListItem.HvacThermostatItem)
      is IconValueListItemViewHolder -> holder.bind(item as ListItem.IconValueItem)
      is IconWithButtonsItemViewHolder -> holder.bind(item as ListItem.IconWithButtonsItem)
      is DoubleValueListItemViewHolder -> holder.bind(item as ListItem.DoubleValueItem)
      is IconWithRightButtonItemViewHolder -> holder.bind(item as ListItem.IconWithRightButtonItem)
      else -> super.onBindViewHolder(holder, position)
    }
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  private fun onLongPress(viewHolder: ViewHolder): Boolean {
    vibrationHelper.vibrate()
    callback.closeWhenSwiped()
    itemTouchHelper.startDrag(viewHolder)

    return true
  }

  inner class ChannelListItemViewHolder(val binding: LiChannelItemBinding) : ViewHolder(binding.root) {
    fun bind(item: ListItem.ChannelItem) {
      binding.channelLayout.setChannelData(item.legacyBase)
      binding.channelLayout.setLocationCaption(item.channelBase.locationCaption)
      binding.channelLayout.setOnLongClickListener { onLongPress(this) }
      binding.channelLayout.setOnClickListener { listItemClickCallback(item.channelBase.remoteId) }
      binding.channelLayout.setInfoIconClickListener { infoButtonClickCallback(item.channelBase.remoteId) }
      binding.channelLayout.onLeftButtonClick = OnClick { onLeftButtonClick(item.channelBase.remoteId) }
      binding.channelLayout.onRightButtonClick = OnClick { onRightButtonClick(item.channelBase.remoteId) }
      binding.channelLayout.onItemClick = OnClick { listItemClickCallback(item.channelBase.remoteId) }
      binding.channelLayout.onCaptionLongPressed = OnClick {
        captionLongPressCallback(item.channelBase.remoteId, item.channelBase.profileId, item.channelBase.caption)
      }
    }
  }

  inner class ThermostatListItemViewHolder(val binding: LiMainThermostatItemBinding) : ViewHolder(binding.root) {
    fun bind(item: ListItem.HvacThermostatItem) {
      val data = item.toSlideableListItemData() as SlideableListItemData.Thermostat
      binding.listItemRoot.bind(locationCaption = item.locationCaption, function = item.channelBase.function)
      binding.listItemContent.bind(
        itemType = ItemType.CHANNEL,
        remoteId = item.channel.remoteId,
        data = data,
        onInfoClick = { infoButtonClickCallback(item.channel.remoteId) },
        onIssueClick = { issueButtonClickCallback(it) },
        onTitleLongClick = { captionLongPressCallback(item.channel.remoteId, item.channel.profileId, item.channel.caption) },
        onItemClick = { listItemClickCallback(item.channel.remoteId) }
      )

      binding.listItemContent.setOnClickListener { listItemClickCallback(item.channel.remoteId) }
      binding.listItemContent.setOnLongClickListener { onLongPress(this) }
      binding.listItemLeftItem.setOnClickListener { onLeftButtonClick(item.channel.remoteId) }
      binding.listItemRightItem.setOnClickListener { onRightButtonClick(item.channel.remoteId) }
    }
  }

  inner class IconValueListItemViewHolder(val binding: LiMainIconValueItemBinding) : ViewHolder(binding.root) {
    fun bind(item: ListItem.IconValueItem) {
      val data = item.toSlideableListItemData() as SlideableListItemData.Default
      binding.listItemRoot.bind(locationCaption = item.locationCaption, function = item.channelBase.function)
      binding.listItemContent.bind(
        itemType = ItemType.CHANNEL,
        remoteId = item.channel.remoteId,
        data = data,
        onInfoClick = { infoButtonClickCallback(item.channel.remoteId) },
        onIssueClick = { issueButtonClickCallback(it) },
        onTitleLongClick = { captionLongPressCallback(item.channel.remoteId, item.channel.profileId, item.channel.caption) },
        onItemClick = { listItemClickCallback(item.channel.remoteId) }
      )

      binding.listItemContent.setOnClickListener { listItemClickCallback(item.channel.remoteId) }
      binding.listItemContent.setOnLongClickListener { onLongPress(this) }
    }
  }

  inner class IconWithButtonsItemViewHolder(val binding: LiMainIconValueWithButtonsItemBinding) : ViewHolder(binding.root) {
    fun bind(item: ListItem.IconWithButtonsItem) {
      val data = item.toSlideableListItemData() as SlideableListItemData.Default
      binding.listItemRoot.bind(locationCaption = item.locationCaption, function = item.channelBase.function)
      binding.listItemContent.bind(
        itemType = ItemType.CHANNEL,
        remoteId = item.channel.remoteId,
        data = data,
        onInfoClick = { infoButtonClickCallback(item.channel.remoteId) },
        onIssueClick = { issueButtonClickCallback(it) },
        onTitleLongClick = { captionLongPressCallback(item.channel.remoteId, item.channel.profileId, item.channel.caption) },
        onItemClick = { listItemClickCallback(item.channel.remoteId) }
      )

      binding.listItemContent.setOnClickListener { listItemClickCallback(item.channel.remoteId) }
      binding.listItemContent.setOnLongClickListener { onLongPress(this) }
      binding.listItemLeftItem.setOnClickListener { onLeftButtonClick(item.channel.remoteId) }
      binding.listItemRightItem.setOnClickListener { onRightButtonClick(item.channel.remoteId) }
    }
  }

  inner class IconWithRightButtonItemViewHolder(val binding: LiMainIconValueWithRightButtonItemBinding) : ViewHolder(binding.root) {
    fun bind(item: ListItem.IconWithRightButtonItem) {
      val data = item.toSlideableListItemData() as SlideableListItemData.Default
      binding.listItemRoot.bind(locationCaption = item.locationCaption, function = item.channelBase.function)
      binding.listItemContent.bind(
        itemType = ItemType.CHANNEL,
        remoteId = item.channel.remoteId,
        data = data,
        onInfoClick = { infoButtonClickCallback(item.channel.remoteId) },
        onIssueClick = { issueButtonClickCallback(it) },
        onTitleLongClick = { captionLongPressCallback(item.channel.remoteId, item.channel.profileId, item.channel.caption) },
        onItemClick = { listItemClickCallback(item.channel.remoteId) }
      )

      binding.listItemContent.setOnClickListener { listItemClickCallback(item.channel.remoteId) }
      binding.listItemContent.setOnLongClickListener { onLongPress(this) }
      binding.listItemRightItem.setOnClickListener { onRightButtonClick(item.channel.remoteId) }
    }
  }

  inner class DoubleValueListItemViewHolder(val binding: LiMainDoubleValueItemBinding) : ViewHolder(binding.root) {
    fun bind(item: ListItem.DoubleValueItem) {
      val data = item.toSlideableListItemData() as SlideableListItemData.DoubleValue
      binding.listItemRoot.bind(locationCaption = item.locationCaption, function = item.channelBase.function)
      binding.listItemContent.bind(
        itemType = ItemType.CHANNEL,
        remoteId = item.channel.remoteId,
        data = data,
        onInfoClick = { infoButtonClickCallback(item.channel.remoteId) },
        onIssueClick = { issueButtonClickCallback(it) },
        onTitleLongClick = { captionLongPressCallback(item.channel.remoteId, item.channel.profileId, item.channel.caption) },
        onItemClick = { listItemClickCallback(item.channel.remoteId) }
      )

      binding.listItemContent.setOnClickListener { listItemClickCallback(item.channel.remoteId) }
      binding.listItemContent.setOnLongClickListener { onLongPress(this) }
    }
  }
}
