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
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.databinding.LiChannelItemBinding
import org.supla.android.databinding.LiMainIconValueItemBinding
import org.supla.android.databinding.LiMainIconValueWithButtonsItemBinding
import org.supla.android.databinding.LiMainThermostatItemBinding
import org.supla.android.ui.layouts.ChannelLayout
import org.supla.android.ui.lists.data.SlideableListItemData

abstract class BaseChannelsAdapter(
  private val context: Context,
  preferences: Preferences
) : BaseListAdapter<ListItem, ChannelDataBase>(context, preferences), ChannelLayout.Listener {

  var infoButtonClickCallback: (id: Int) -> Unit = { _ -> }
  var issueButtonClickCallback: (messageId: Int?) -> Unit = { _ -> }
  var listItemClickCallback: (remoteId: Int) -> Unit = { _ -> }

  override val callback = ListCallback(context, this).also {
    it.onMovedListener = { fromPos, toPos -> swapInternally(fromPos, toPos) }
    it.onMoveFinishedListener = {
      val channelsOrdered = items
        .filterIsInstance<ListItem.ChannelItem>()
        .map { sceneItem -> sceneItem.channelBase }

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

      ViewType.GENERAL_PURPOSE_METER_ITEM.ordinal ->
        GpMeterListItemViewHolder(LiMainIconValueItemBinding.inflate(inflater, parent, false))

      ViewType.GENERAL_PURPOSE_MEASUREMENT_ITEM.ordinal ->
        GpMeasurementListItemViewHolder(LiMainIconValueItemBinding.inflate(inflater, parent, false))

      ViewType.SHADING_SYSTEM_ITEM.ordinal ->
        ShadingSystemListItemViewHolder(LiMainIconValueWithButtonsItemBinding.inflate(inflater, parent, false))

      ViewType.SWITCH_ITEM.ordinal ->
        SwitchListItemViewHolder(LiMainIconValueWithButtonsItemBinding.inflate(inflater, parent, false))

      else -> super.onCreateViewHolder(parent, viewType)
    }
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    when (holder) {
      is ChannelListItemViewHolder -> holder.bind(items[position] as ListItem.ChannelItem)
      is ThermostatListItemViewHolder -> holder.bind(items[position] as ListItem.HvacThermostatItem)
      is IconValueListItemViewHolder -> holder.bind(items[position] as ListItem.IconValueItem)
      is GpMeterListItemViewHolder -> holder.bind(items[position] as ListItem.GeneralPurposeMeterItem)
      is GpMeasurementListItemViewHolder -> holder.bind(items[position] as ListItem.GeneralPurposeMeasurementItem)
      is ShadingSystemListItemViewHolder -> holder.bind(items[position] as ListItem.ShadingSystemItem)
      is SwitchListItemViewHolder -> holder.bind(items[position] as ListItem.SwitchItem)
      else -> super.onBindViewHolder(holder, position)
    }
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun onItemClick(remoteId: Int) {
    listItemClickCallback(remoteId)
  }

  private fun onLongPress(viewHolder: ViewHolder): Boolean {
    SuplaApp.Vibrate(context)
    callback.closeWhenSwiped()
    itemTouchHelper.startDrag(viewHolder)

    return true
  }

  inner class ChannelListItemViewHolder(val binding: LiChannelItemBinding) : ViewHolder(binding.root) {
    fun bind(item: ListItem.ChannelItem) {
      binding.channelLayout.setChannelData(item.legacyBase)
      binding.channelLayout.setLocationCaption(item.channelBase.locationCaption)
      binding.channelLayout.setChannelListener(this@BaseChannelsAdapter)
      binding.channelLayout.setOnLongClickListener { onLongPress(this) }
      binding.channelLayout.setOnClickListener { listItemClickCallback(item.channelBase.remoteId) }
      binding.channelLayout.setInfoIconClickListener { infoButtonClickCallback(item.channelBase.remoteId) }
    }
  }

  inner class ThermostatListItemViewHolder(val binding: LiMainThermostatItemBinding) : ViewHolder(binding.root) {
    fun bind(item: ListItem.HvacThermostatItem) {
      val data = item.toSlideableListItemData() as SlideableListItemData.Thermostat
      binding.listItemRoot.bind(locationCaption = item.locationCaption)
      binding.listItemContent.bind(
        itemType = ItemType.CHANNEL,
        remoteId = item.channel.remoteId,
        data = data,
        onInfoClick = { infoButtonClickCallback(item.channel.remoteId) },
        onIssueClick = { issueButtonClickCallback(item.issueMessage) },
        onTitleLongClick = { onCaptionLongPress(item.channel.remoteId) },
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
      binding.listItemRoot.bind(locationCaption = item.locationCaption)
      binding.listItemContent.bind(
        itemType = ItemType.CHANNEL,
        remoteId = item.channel.remoteId,
        data = data,
        onInfoClick = { infoButtonClickCallback(item.channel.remoteId) },
        onIssueClick = { },
        onTitleLongClick = { onCaptionLongPress(item.channel.remoteId) },
        onItemClick = { listItemClickCallback(item.channel.remoteId) }
      )

      binding.listItemContent.setOnClickListener { listItemClickCallback(item.channel.remoteId) }
      binding.listItemContent.setOnLongClickListener { onLongPress(this) }
    }
  }

  inner class GpMeterListItemViewHolder(val binding: LiMainIconValueItemBinding) : ViewHolder(binding.root) {
    fun bind(item: ListItem.GeneralPurposeMeterItem) {
      val data = item.toSlideableListItemData() as SlideableListItemData.Default
      binding.listItemRoot.bind(locationCaption = item.locationCaption)
      binding.listItemContent.bind(
        itemType = ItemType.CHANNEL,
        remoteId = item.channel.remoteId,
        data = data,
        onInfoClick = { infoButtonClickCallback(item.channel.remoteId) },
        onIssueClick = { },
        onTitleLongClick = { onCaptionLongPress(item.channel.remoteId) },
        onItemClick = { listItemClickCallback(item.channel.remoteId) }
      )

      binding.listItemContent.setOnClickListener { listItemClickCallback(item.channel.remoteId) }
      binding.listItemContent.setOnLongClickListener { onLongPress(this) }
    }
  }

  inner class GpMeasurementListItemViewHolder(val binding: LiMainIconValueItemBinding) : ViewHolder(binding.root) {
    fun bind(item: ListItem.GeneralPurposeMeasurementItem) {
      val data = item.toSlideableListItemData() as SlideableListItemData.Default
      binding.listItemRoot.bind(locationCaption = item.locationCaption)
      binding.listItemContent.bind(
        itemType = ItemType.CHANNEL,
        remoteId = item.channel.remoteId,
        data = data,
        onInfoClick = { infoButtonClickCallback(item.channel.remoteId) },
        onIssueClick = { },
        onTitleLongClick = { onCaptionLongPress(item.channel.remoteId) },
        onItemClick = { listItemClickCallback(item.channel.remoteId) }
      )

      binding.listItemContent.setOnClickListener { listItemClickCallback(item.channel.remoteId) }
      binding.listItemContent.setOnLongClickListener { onLongPress(this) }
    }
  }

  inner class ShadingSystemListItemViewHolder(val binding: LiMainIconValueWithButtonsItemBinding) : ViewHolder(binding.root) {
    fun bind(item: ListItem.ShadingSystemItem) {
      val data = item.toSlideableListItemData() as SlideableListItemData.Default
      binding.listItemRoot.bind(locationCaption = item.locationCaption)
      binding.listItemContent.bind(
        itemType = ItemType.CHANNEL,
        remoteId = item.channel.remoteId,
        data = data,
        onInfoClick = { infoButtonClickCallback(item.channel.remoteId) },
        onIssueClick = { issueButtonClickCallback(item.issueMessage) },
        onTitleLongClick = { onCaptionLongPress(item.channel.remoteId) },
        onItemClick = { listItemClickCallback(item.channel.remoteId) }
      )

      binding.listItemContent.setOnClickListener { listItemClickCallback(item.channel.remoteId) }
      binding.listItemContent.setOnLongClickListener { onLongPress(this) }
      binding.listItemLeftItem.setOnClickListener { onLeftButtonClick(item.channel.remoteId) }
      binding.listItemLeftItem.setText(R.string.channel_btn_close)
      binding.listItemRightItem.setOnClickListener { onRightButtonClick(item.channel.remoteId) }
      binding.listItemRightItem.setText(R.string.channel_btn_open)
    }
  }

  inner class SwitchListItemViewHolder(val binding: LiMainIconValueWithButtonsItemBinding) : ViewHolder(binding.root) {
    fun bind(item: ListItem.SwitchItem) {
      val data = item.toSlideableListItemData() as SlideableListItemData.Default
      binding.listItemRoot.bind(locationCaption = item.locationCaption)
      binding.listItemContent.bind(
        itemType = ItemType.CHANNEL,
        remoteId = item.channel.remoteId,
        data = data,
        onInfoClick = { infoButtonClickCallback(item.channel.remoteId) },
        onIssueClick = { issueButtonClickCallback(item.issueMessage) },
        onTitleLongClick = { onCaptionLongPress(item.channel.remoteId) },
        onItemClick = { listItemClickCallback(item.channel.remoteId) }
      )

      binding.listItemContent.setOnClickListener { listItemClickCallback(item.channel.remoteId) }
      binding.listItemContent.setOnLongClickListener { onLongPress(this) }
      binding.listItemLeftItem.setOnClickListener { onLeftButtonClick(item.channel.remoteId) }
      binding.listItemLeftItem.setText(R.string.channel_btn_off)
      binding.listItemRightItem.setOnClickListener { onRightButtonClick(item.channel.remoteId) }
      binding.listItemRightItem.setText(R.string.channel_btn_on)
    }
  }
}
