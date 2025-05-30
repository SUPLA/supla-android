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

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.TypedValue
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.ItemTouchHelperVariables
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import org.supla.android.R

@SuppressLint("ClickableViewAccessibility")
class ListCallback(
  private val context: Context,
  private val adapter: Adapter<ViewHolder>
) : ItemTouchHelper.SimpleCallback(
  ItemTouchHelper.UP or ItemTouchHelper.DOWN,
  ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {

  private val buttonWidth =
    context.resources.getDimensionPixelSize(R.dimen.channel_layout_button_width)
  private val reorderingElevation = 15.toPx().toFloat()

  var onMovedListener: (fromPos: Int, toPos: Int) -> Unit = { _: Int, _: Int -> }
  var onMoveFinishedListener: () -> Unit = { }

  private var swipeBack = false
  private var state: ItemState = ItemState.Closed
  private var lastActivePosition = 0f
  private var lastActiveFlag = false
  private var moved = false

  private var onScrollListener = object : OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
      if (newState == SCROLL_STATE_DRAGGING || newState == SCROLL_STATE_SETTLING) {
        closeWhenSwiped()
      }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {}
  }

  fun setup(recyclerView: RecyclerView) {
    recyclerView.setOnTouchListener { _: View, event: MotionEvent ->
      swipeBack = event.action == ACTION_CANCEL || event.action == ACTION_UP
      false
    }
    recyclerView.addOnScrollListener(onScrollListener)
  }

  override fun getMovementFlags(
    recyclerView: RecyclerView,
    viewHolder: ViewHolder
  ): Int {
    if (viewHolder is BaseListAdapter.LocationListItemViewHolder) {
      return 0
    }
    return super.getMovementFlags(recyclerView, viewHolder)
  }

  override fun onMove(
    recyclerView: RecyclerView,
    viewHolder: ViewHolder,
    target: ViewHolder
  ): Boolean {
    adapter.notifyItemMoved(viewHolder.absoluteAdapterPosition, target.absoluteAdapterPosition)
    return true
  }

  override fun onMoved(
    recyclerView: RecyclerView,
    viewHolder: ViewHolder,
    fromPos: Int,
    target: ViewHolder,
    toPos: Int,
    x: Int,
    y: Int
  ) {
    moved = true
    onMovedListener(fromPos, toPos)
    super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
  }

  override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
    if (moved) {
      onMoveFinishedListener()
      moved = false

      viewHolder.itemView.translationX = lastActivePosition
    }
  }

  override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
  }

  override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
    if (swipeBack) {
      swipeBack = false
      return flags and ItemTouchHelperVariables.ACTION_MODE_DRAG_MASK
    }
    return super.convertToAbsoluteDirection(flags, layoutDirection)
  }

  override fun onChildDraw(
    c: Canvas,
    recyclerView: RecyclerView,
    viewHolder: ViewHolder,
    dX: Float,
    dY: Float,
    actionState: Int,
    isCurrentlyActive: Boolean
  ) {
    if (isCurrentlyActive) {
      // If in active movement there is swiped state, it means something is swiped, so try to close that
      if (state is ItemState.Swiped && (state as ItemState.Swiped).view != viewHolder.itemView) {
        closeItem()
      }
    }

    var correctedX = dX
    if (actionState == ACTION_STATE_SWIPE) {
      if (state is ItemState.Swiped) {
        if (state.position == ItemPosition.SWIPED_RIGHT) {
          correctedX += buttonWidth.toFloat()
        } else {
          correctedX -= buttonWidth.toFloat()
        }
      }

      when {
        isCurrentlyActive -> {
          lastActivePosition = correctedX
        }
        lastActiveFlag -> {
          state = calculateState(viewHolder)
          animateOnFingerRelease(viewHolder.itemView)
        }
      }
    }
    if (actionState == ACTION_STATE_DRAG) {
      super.onChildDraw(c, recyclerView, viewHolder, 0f, dY, actionState, isCurrentlyActive)
    } else if (isCurrentlyActive) {
      (viewHolder.itemView as SlideableItem).slide(correctedX.toInt())
    }

    lastActiveFlag = isCurrentlyActive

    // Elevation change need to be made after onChildDraw, because there is other logic which makes changes
    if (isCurrentlyActive && actionState == ACTION_STATE_DRAG) {
      viewHolder.itemView.elevation = reorderingElevation
    } else {
      viewHolder.itemView.elevation = 0f
    }
  }

  override fun isLongPressDragEnabled(): Boolean {
    return false
  }

  override fun canDropOver(
    recyclerView: RecyclerView,
    current: ViewHolder,
    target: ViewHolder
  ): Boolean {
    val currentView = current.itemView
    val targetView = target.itemView

    return (currentView as? SwapableListItem)?.canSwap(targetView) ?: false
  }

  internal fun closeWhenSwiped(withAnimation: Boolean = true) {
    if (state !is ItemState.Swiped) {
      return
    }

    closeItem(withAnimation)
  }

  private fun closeItem(withAnimation: Boolean = true) {
    (state as ItemState.Swiped).also {
      if (withAnimation) {
        startAnimation(it.view, it.dX, 0f)
      } else {
        it.view.translationX = 0f
        it.view.invalidate()
      }
    }
    state = ItemState.Closed
  }

  private fun calculateState(viewHolder: ViewHolder) = when {
    state.position == ItemPosition.CLOSED && lastActivePosition > buttonWidth / 2 ->
      newState(viewHolder, buttonWidth.toFloat(), ItemPosition.SWIPED_RIGHT)
    state.position == ItemPosition.CLOSED && lastActivePosition < -buttonWidth / 2 ->
      newState(viewHolder, -buttonWidth.toFloat(), ItemPosition.SWIPED_LEFT)
    state.position == ItemPosition.SWIPED_RIGHT && lastActivePosition < -buttonWidth / 2 ->
      newState(viewHolder, -buttonWidth.toFloat(), ItemPosition.SWIPED_LEFT)
    state.position == ItemPosition.SWIPED_RIGHT && lastActivePosition < buttonWidth / 2 ->
      ItemState.Closed
    state.position == ItemPosition.SWIPED_LEFT && lastActivePosition > buttonWidth / 2 ->
      newState(viewHolder, buttonWidth.toFloat(), ItemPosition.SWIPED_RIGHT)
    state.position == ItemPosition.SWIPED_LEFT && lastActivePosition > -buttonWidth / 2 ->
      ItemState.Closed
    else -> state
  }

  private fun newState(vh: ViewHolder, dX: Float, position: ItemPosition) =
    ItemState.Swiped(vh.itemView, dX, position)

  private fun animateOnFingerRelease(view: View) {
    val destination = when (state.position) {
      ItemPosition.CLOSED -> 0f
      ItemPosition.SWIPED_RIGHT -> buttonWidth.toFloat()
      ItemPosition.SWIPED_LEFT -> -buttonWidth.toFloat()
    }

    startAnimation(view, lastActivePosition, destination)
  }

  private fun startAnimation(
    view: View,
    fromPos: Float,
    toPos: Float
  ) {
    val holderName = "percentage"
    val valuesHolder = PropertyValuesHolder.ofFloat(holderName, fromPos, toPos)
    val animator = ValueAnimator().apply {
      setValues(valuesHolder)
      duration = 200
      interpolator = AccelerateDecelerateInterpolator()
      addUpdateListener {
        val x = it.getAnimatedValue(holderName) as Float
        (view as SlideableItem).slide(x.toInt())
      }
      if (toPos == 0f) {
        addListener { doOnEnd { view.translationX = 0f } }
      }
    }
    animator.start()
  }

  fun Int.toPx(): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), context.resources.displayMetrics).toInt()
  }
}

enum class ItemPosition {
  CLOSED, SWIPED_LEFT, SWIPED_RIGHT
}

sealed class ItemState(val position: ItemPosition) {
  object Closed : ItemState(ItemPosition.CLOSED)
  class Swiped(
    val view: View,
    val dX: Float,
    position: ItemPosition
  ) :
    ItemState(position)
}
