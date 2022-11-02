package org.supla.android.scenes

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Region
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import org.supla.android.R
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class ScenesListCallback(private val adapter: ScenesAdapter) : ItemTouchHelper.SimpleCallback(
  ItemTouchHelper.UP or ItemTouchHelper.DOWN,
  ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {

  private lateinit var leftButton: ActionButton
  private lateinit var rightButton: ActionButton

  var leftButtonClickedListener: (sceneId: Int) -> Unit = { _: Int -> }
  var rightButtonClickedListener: (sceneId: Int) -> Unit = { _: Int -> }
  var onMovedListener: (fromPos: Int, toPos: Int) -> Unit = { _: Int, _: Int -> }
  var onMoveFinishedListener: () -> Unit = { }

  private var swipeBack = false
  private var state: ItemState = ItemState.Closed
  private var previousPosition = ItemPosition.CLOSED
  private var evaluatingClick = false
  private var lastActivePosition = 0f
  private var lastActiveFlag = false
  private var moved = false

  private var onScrollListener = object : OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
      if (newState == SCROLL_STATE_DRAGGING || newState == SCROLL_STATE_SETTLING) {
        closeWhenSwiped(recyclerView)
      }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {}
  }

  fun setup(recyclerView: RecyclerView) {
    recyclerView.setOnTouchListener { _: View, event: MotionEvent ->
      if (handleTouchEvent(recyclerView, event)) {
        true
      } else {
        swipeBack = event.action == ACTION_CANCEL || event.action == ACTION_UP
        false
      }
    }
    recyclerView.addOnScrollListener(onScrollListener)

    val context = recyclerView.context
    leftButton =
      ActionButton(context, ActionButton.Position.LEFT, context.getString(R.string.btn_abort))
    rightButton =
      ActionButton(context, ActionButton.Position.RIGHT, context.getString(R.string.btn_execute))
  }

  override fun getMovementFlags(
    recyclerView: RecyclerView,
    viewHolder: ViewHolder
  ): Int {
    if (viewHolder is ScenesAdapter.LocationListItemViewHolder) {
      return 0
    }
    return super.getMovementFlags(recyclerView, viewHolder)
  }

  override fun canDropOver(
    recyclerView: RecyclerView,
    current: ViewHolder,
    target: ViewHolder
  ): Boolean {
    val currentView = current.itemView
    val targetView = target.itemView

    if (currentView !is SceneLayout || targetView !is SceneLayout) {
      return false
    }
    return currentView.locationId == targetView.locationId
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
    super.clearView(recyclerView, viewHolder)
    if (moved) {
      onMoveFinishedListener()
      moved = false
    }
  }

  override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
  }

  override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
    if (swipeBack) {
      swipeBack = false
      return 0
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
      if (state is ItemState.Swiped && (state as ItemState.Swiped).holder != viewHolder) {
        closeItem(recyclerView)
      }
    }

    var correctedX = dX
    if (actionState == ACTION_STATE_SWIPE) {
      if (state is ItemState.Swiped) {
        if (state.position == ItemPosition.SWIPED_RIGHT) {
          correctedX += leftButton.getButtonWidth().toFloat()
        } else {
          correctedX -= rightButton.getButtonWidth().toFloat()
        }
      }

      when {
        isCurrentlyActive -> {
          lastActivePosition = correctedX
        }
        lastActiveFlag -> {
          previousPosition = state.position
          state = calculateState(viewHolder, c, dY, correctedX)
        }
        else -> {
          correctedX = calculateCorrectionWhenItemReleased(dX)
        }
      }
    }
    if (actionState == ACTION_STATE_DRAG) {
      correctedX = 0f
    }

    super.onChildDraw(c, recyclerView, viewHolder, correctedX, dY, actionState, isCurrentlyActive)
    drawButtons(viewHolder, correctedX, c)

    lastActiveFlag = isCurrentlyActive
  }

  override fun isLongPressDragEnabled(): Boolean {
    return false
  }

  private fun createClickableRegion(xPosition: Float, itemView: View): Region {
    return if (xPosition < 0) {
      Region(
        itemView.right - rightButton.getButtonWidth(),
        itemView.top,
        itemView.right,
        itemView.bottom
      )
    } else {
      Region(
        itemView.left,
        itemView.top,
        itemView.left + leftButton.getButtonWidth(),
        itemView.bottom
      )
    }
  }

  internal fun closeWhenSwiped(recyclerView: RecyclerView) {
    if (state !is ItemState.Swiped) {
      return
    }

    closeItem(recyclerView)
  }

  private fun closeItem(recyclerView: RecyclerView) {
    (state as ItemState.Swiped).also {
      super.onChildDraw(it.canvas, recyclerView, it.holder, 0f, it.dY, 0, false)
    }
    state = ItemState.Closed
  }

  private fun handleTouchEvent(recyclerView: RecyclerView, event: MotionEvent): Boolean {
    if (state is ItemState.Swiped) {
      val swipedState = (state as ItemState.Swiped)
      val region = swipedState.clickableRegion
      if (event.action == ACTION_DOWN && region.contains(event)) {
        evaluatingClick = true
        return true
      } else if (evaluatingClick && event.action == ACTION_MOVE && !region.contains(event)) {
        evaluatingClick = false
      } else if (evaluatingClick && event.action == ACTION_UP && region.contains(event)) {
        evaluatingClick = false
        if (swipedState.position == ItemPosition.SWIPED_RIGHT) {
          leftButtonClickedListener(swipedState.holder.itemView.tag as Int)
        } else {
          rightButtonClickedListener(swipedState.holder.itemView.tag as Int)
        }
        closeItem(recyclerView)
        return true
      } else if (evaluatingClick && event.action == ACTION_CANCEL) {
        evaluatingClick = false
      }
    }

    return false
  }

  private fun calculateState(viewHolder: ViewHolder, c: Canvas, dY: Float, dX: Float) = when {
    state.position == ItemPosition.CLOSED && lastActivePosition > leftButton.getButtonWidth() / 2 ->
      newState(viewHolder, c, dY, dX, ItemPosition.SWIPED_RIGHT)
    state.position == ItemPosition.CLOSED && lastActivePosition < -rightButton.getButtonWidth() / 2 ->
      newState(viewHolder, c, dY, dX, ItemPosition.SWIPED_LEFT)
    state.position == ItemPosition.SWIPED_RIGHT && lastActivePosition < -rightButton.getButtonWidth() / 2 ->
      newState(viewHolder, c, dY, dX, ItemPosition.SWIPED_LEFT)
    state.position == ItemPosition.SWIPED_RIGHT && lastActivePosition < leftButton.getButtonWidth() / 2 ->
      ItemState.Closed
    state.position == ItemPosition.SWIPED_LEFT && lastActivePosition > leftButton.getButtonWidth() / 2 ->
      newState(viewHolder, c, dY, dX, ItemPosition.SWIPED_RIGHT)
    state.position == ItemPosition.SWIPED_LEFT && lastActivePosition > -rightButton.getButtonWidth() / 2 ->
      ItemState.Closed
    else -> state
  }

  private fun newState(vh: ViewHolder, c: Canvas, dY: Float, dX: Float, position: ItemPosition) =
    ItemState.Swiped(vh, c, dY, createClickableRegion(dX, vh.itemView), position)

  private fun calculateCorrectionWhenItemReleased(dX: Float): Float {
    val destination = when (state.position) {
      ItemPosition.CLOSED -> 0f
      ItemPosition.SWIPED_RIGHT -> leftButton.getButtonWidth().toFloat()
      ItemPosition.SWIPED_LEFT -> -rightButton.getButtonWidth().toFloat()
    }

    return when {
      // Was opened to the right, and is going to be opened on the left side (short movement, not whole button shown)
      previousPosition == ItemPosition.SWIPED_RIGHT && state.position == ItemPosition.SWIPED_LEFT
        && abs(lastActivePosition) < rightButton.getButtonWidth()
        && abs(lastActivePosition) > rightButton.getButtonWidth() * 0.5 -> {
        val scale = dX / (rightButton.getButtonWidth() + abs(lastActivePosition))
        val road = (rightButton.getButtonWidth() - abs(lastActivePosition))
        val dest = -rightButton.getButtonWidth()

        dest - road * scale
      }
      // Was opened to the left, and is going to be opened on the right side (short movement, not whole button shown)
      previousPosition == ItemPosition.SWIPED_LEFT && state.position == ItemPosition.SWIPED_RIGHT
        && abs(lastActivePosition) < leftButton.getButtonWidth()
        && abs(lastActivePosition) > leftButton.getButtonWidth() * 0.5 -> {
        val scale = dX / (leftButton.getButtonWidth() + abs(lastActivePosition))
        val road = (leftButton.getButtonWidth() - lastActivePosition)
        val dest = leftButton.getButtonWidth()

        dest - road * scale
      }
      // Was opened to left and is going to be closed - same side
      previousPosition == ItemPosition.SWIPED_LEFT && state.position == ItemPosition.CLOSED
        && lastActivePosition < 0
        && abs(lastActivePosition) < rightButton.getButtonWidth() * 0.5
        && abs(lastActivePosition) > 0 -> {
        val scale = dX / (rightButton.getButtonWidth() - abs(lastActivePosition))
        val road = (lastActivePosition)
        val dest = 0

        dest + road * scale
      }
      // Was opened to left and is going to be closed - opposite side
      previousPosition == ItemPosition.SWIPED_LEFT && state.position == ItemPosition.CLOSED
        && lastActivePosition > 0
        && abs(lastActivePosition) < leftButton.getButtonWidth() * 0.5
        && abs(lastActivePosition) > 0 -> {
        val scale = dX / (leftButton.getButtonWidth() + abs(lastActivePosition))
        val road = (lastActivePosition)
        val dest = 0

        dest + road * scale
      }
      // Was opened to right and is going to be closed - same side
      previousPosition == ItemPosition.SWIPED_RIGHT && state.position == ItemPosition.CLOSED
        && lastActivePosition > 0
        && lastActivePosition < leftButton.getButtonWidth() * 0.5
        && lastActivePosition > 0 -> {
        val scale = dX / (leftButton.getButtonWidth() - abs(lastActivePosition))
        val road = (lastActivePosition)
        val dest = 0

        dest - road * scale
      }
      // Was opened to right and is going to be closed - opposite side
      previousPosition == ItemPosition.SWIPED_RIGHT && state.position == ItemPosition.CLOSED
        && lastActivePosition < 0
        && abs(lastActivePosition) < leftButton.getButtonWidth() * 0.5
        && abs(lastActivePosition) > 0 -> {
        val scale = dX / (leftButton.getButtonWidth() + abs(lastActivePosition))
        val road = (lastActivePosition)
        val dest = 0

        dest - road * scale
      }
      // Was opened and stays opened, closing movement, but to short (both sides)
      (previousPosition == ItemPosition.SWIPED_RIGHT ||
        previousPosition == ItemPosition.SWIPED_LEFT)
        && abs(lastActivePosition) > abs(destination) * 0.5
        && abs(lastActivePosition) < abs(destination) -> destination + dX
      // Was opened and stays opened on the same side, movement far away from button (both sides)
      state.position == ItemPosition.SWIPED_RIGHT && lastActivePosition > destination ||
        state.position == ItemPosition.SWIPED_LEFT && lastActivePosition < destination -> {
        val result = dX + destination
        if (abs(result) < abs(lastActivePosition)) {
          result
        } else {
          lastActivePosition
        }
      }
      // Was closed and is going to be opened, but movement was shorter then button (both sides)
      state.position == ItemPosition.SWIPED_RIGHT && lastActivePosition < destination ||
        state.position == ItemPosition.SWIPED_LEFT && lastActivePosition > destination -> {
        val result = destination - dX
        if (abs(result) < abs(lastActivePosition)) {
          lastActivePosition
        } else {
          result
        }
      }
      // Was closed and is not swiped enough to be opened, so going back to closed (both sides)
      else -> dX
    }
  }

  private fun drawButtons(viewHolder: ViewHolder, correctedX: Float, canvas: Canvas) {
    val view: View = viewHolder.itemView
    if (correctedX > 0) {
      val area = Rect(view.left, view.top, correctedX.toInt(), view.bottom)
      leftButton.draw(canvas, area)
    } else if (correctedX < 0) {
      val area = Rect(view.right + correctedX.toInt(), view.top, view.right, view.bottom)
      rightButton.draw(canvas, area)
    }
  }
}

enum class ItemPosition {
  CLOSED, SWIPED_LEFT, SWIPED_RIGHT
}

sealed class ItemState(val position: ItemPosition) {
  object Closed : ItemState(ItemPosition.CLOSED)
  class Swiped(
    val holder: ViewHolder,
    val canvas: Canvas,
    val dY: Float,
    val clickableRegion: Region,
    position: ItemPosition
  ) :
    ItemState(position)
}

fun Region.contains(event: MotionEvent): Boolean = contains(event.x.toInt(), event.y.toInt())