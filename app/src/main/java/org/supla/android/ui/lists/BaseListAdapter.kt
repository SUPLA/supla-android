package org.supla.android.ui.lists

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.databinding.LiLocationItemBinding
import org.supla.android.db.Location
import org.supla.android.ui.dialogs.LocationCaptionEditor

abstract class BaseListAdapter<T, D>(
  private val context: Context,
  private val preferences: Preferences
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  protected val items: MutableList<T> = mutableListOf()
  protected abstract val callback: ListCallback
  protected val itemTouchHelper by lazy { ItemTouchHelper(callback) }

  var movementFinishedCallback: (items: List<D>) -> Unit = { }
  var swappedElementsCallback: (firstItem: D?, secondItem: D?) -> Unit = { _, _ -> }
  var reloadCallback: () -> Unit = { }
  var toggleLocationCallback: (location: Location, scrollDown: Boolean) -> Unit = { _, _ -> }

  var leftButtonClickCallback: (id: Int) -> Unit = { _ -> }
  var rightButtonClickCallback: (id: Int) -> Unit = { _ -> }

  private var movedStartPosition: Int? = null
  protected var movedItem: T? = null
  protected var replacedItem: T? = null

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)

    itemTouchHelper.attachToRecyclerView(recyclerView)
    callback.setup(recyclerView)
  }

  override fun getItemCount(): Int {
    return items.count()
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      R.layout.li_location_item -> LocationListItemViewHolder(LiLocationItemBinding.inflate(inflater, parent, false))
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
        holder.binding.container.setOnLongClickListener { changeLocationCaption(location.locationId) }
        holder.binding.tvSectionCaption.text = location.caption
        holder.binding.ivSectionCollapsed.visibility = if (isLocationCollapsed(location)) {
          RecyclerView.VISIBLE
        } else {
          View.GONE
        }
      }
    }
  }

  protected abstract fun isLocationCollapsed(location: Location): Boolean

  fun setItems(items: List<T>) {
    this.items.clear()
    this.items.addAll(items)
    notifyDataSetChanged()
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

  private fun changeLocationCaption(locationId: Int): Boolean {
    SuplaApp.Vibrate(context)
    val editor = LocationCaptionEditor(context)
    editor.captionChangedListener = reloadCallback
    editor.edit(locationId)

    return true
  }

  class LocationListItemViewHolder(val binding: LiLocationItemBinding) :
    RecyclerView.ViewHolder(binding.root)

  private fun View.isLocationOnBottom(): Boolean {
    val parentAsGroup = parent as? ViewGroup ?: return false
    return y + height >= parentAsGroup.height
  }
}
