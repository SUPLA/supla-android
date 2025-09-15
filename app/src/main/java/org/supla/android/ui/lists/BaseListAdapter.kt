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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.supla.android.Preferences
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.databinding.LiLocationItemBinding
import kotlin.math.max

abstract class BaseListAdapter<D>(
  private val preferences: Preferences
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  protected val items: MutableList<ListItem> = mutableListOf()
  protected abstract val callback: ListCallback
  protected val itemTouchHelper by lazy { ItemTouchHelper(callback) }

  var movementFinishedCallback: (items: List<D>) -> Unit = { }
  var swappedElementsCallback: (firstItem: D?, secondItem: D?) -> Unit = { _, _ -> }
  var toggleLocationCallback: (location: LocationEntity, scrollDown: Boolean) -> Unit = { _, _ -> }

  var leftButtonClickCallback: (id: Int) -> Unit = { _ -> }
  var rightButtonClickCallback: (id: Int) -> Unit = { _ -> }
  var captionLongPressCallback: (remoteId: Int, profileId: Long, caption: String) -> Unit = { _, _, _ -> }
  var locationCaptionLongPressCallback: (remoteId: Int, profileId: Long, caption: String) -> Unit = { _, _, _ -> }

  private var movedStartPosition: Int? = null
  protected var movedItem: ListItem? = null
  protected var replacedItem: ListItem? = null

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)

    itemTouchHelper.attachToRecyclerView(recyclerView)
    callback.setup(recyclerView)
  }

  override fun getItemCount(): Int {
    return items.count()
  }

  override fun getItemViewType(pos: Int): Int {
    return when (items[pos]) {
      is ListItem.ChannelItem -> ViewType.CHANNEL_ITEM
      is ListItem.HvacThermostatItem -> ViewType.HVAC_ITEM
      is ListItem.HeatpolThermostatItem -> ViewType.HEATPOL_ITEM
      is ListItem.IconValueItem -> ViewType.ICON_VALUE_ITEM
      is ListItem.SceneItem -> ViewType.SCENE_ITEM
      is ListItem.LocationItem -> ViewType.LOCATION_ITEM
      is ListItem.IconWithButtonsItem -> ViewType.ICON_WITH_BUTTONS_ITEM
      is ListItem.DoubleValueItem -> ViewType.DOUBLE_VALUE_ITEM
      is ListItem.IconWithRightButtonItem -> ViewType.ICON_WITH_RIGHT_BUTTON_ITEM

      else -> throw IllegalStateException("Could find evaluate view item type")
    }.ordinal
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      ViewType.LOCATION_ITEM.ordinal -> LocationListItemViewHolder(LiLocationItemBinding.inflate(inflater, parent, false))
      else -> throw IllegalArgumentException("unsupported view type $viewType")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is LocationListItemViewHolder -> {
        val location = (items[position] as ListItem.LocationItem).location
        holder.binding.container.setOnClickListener {
          callback.closeWhenSwiped(withAnimation = false)
          toggleLocationCallback(location, it.isLocationOnBottom())
        }
        holder.binding.container.setOnLongClickListener {
          locationCaptionLongPressCallback(location.remoteId, location.profileId, location.caption)
          return@setOnLongClickListener true
        }
        holder.binding.tvSectionCaption.text = location.caption
        holder.binding.ivSectionCollapsed.visibility = if (isLocationCollapsed(location)) {
          RecyclerView.VISIBLE
        } else {
          View.GONE
        }
      }
    }
  }

  protected abstract fun isLocationCollapsed(location: LocationEntity): Boolean

  fun setItems(items: List<ListItem>) {
    val iterations = max(this.items.count(), items.count())
    var removedItems = 0

    for (index in 0..<iterations) {
      val oldItem = this.items.getOrNull(index - removedItems)
      val newItem = items.getOrNull(index)

      if (oldItem != null && newItem != null) {
        this.items[index] = newItem
        if (oldItem.isDifferentFrom(newItem)) {
          notifyItemChanged(index)
        }
      } else if (oldItem == null) {
        this.items.add(newItem!!)
        notifyItemInserted(index)
      } else {
        this.items.removeAt(index - removedItems)
        notifyItemRemoved(index - removedItems)
        removedItems++
      }
    }
  }

  fun onLeftButtonClick(remoteId: Int) {
    if (preferences.isButtonAutohide) {
      callback.closeWhenSwiped()
    }
    leftButtonClickCallback(remoteId)
  }

  fun onRightButtonClick(remoteId: Int) {
    if (preferences.isButtonAutohide) {
      callback.closeWhenSwiped()
    }
    rightButtonClickCallback(remoteId)
  }

  protected fun swapInternally(fromPos: Int, toPos: Int) {
    if (movedItem == null) {
      movedStartPosition = fromPos
      movedItem = items[fromPos]
    }
    replacedItem = items[toPos]
    if (movedStartPosition == toPos) {
      // moved back to original position
      movedItem = null
      replacedItem = null
    }

    val buf = items[fromPos]
    items[fromPos] = items[toPos]
    items[toPos] = buf
  }

  protected fun cleanSwap() {
    movedItem = null
    replacedItem = null
    movedStartPosition = null
  }

  class LocationListItemViewHolder(val binding: LiLocationItemBinding) :
    RecyclerView.ViewHolder(binding.root)

  enum class ViewType {
    SCENE_ITEM,
    CHANNEL_ITEM,
    LOCATION_ITEM,

    ICON_VALUE_ITEM,
    HVAC_ITEM,
    HEATPOL_ITEM,
    ICON_WITH_BUTTONS_ITEM,
    DOUBLE_VALUE_ITEM,
    ICON_WITH_RIGHT_BUTTON_ITEM
  }

  private fun View.isLocationOnBottom(): Boolean {
    val parentAsGroup = parent as? ViewGroup ?: return false
    return y + height >= parentAsGroup.height
  }
}

fun interface OnClick {
  fun onClick()
}
