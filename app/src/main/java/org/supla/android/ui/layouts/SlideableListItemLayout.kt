package org.supla.android.ui.layouts

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.ifLet
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.SlideableItem
import org.supla.android.ui.lists.SwapableListItem
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.usecases.list.CreateListItemUpdateEventDataUseCase
import java.lang.Integer.min
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * List item which can be swiped left or right.
 * It expects at least one item with list_item_content id. Additionally you can place there two items, when will be placed on the left
 * side of the view and another one on the right side. Using swiping you'll be able to open those elements. The needs to have specified
 * ids - list_item_left_item and list_item_right_item
 */
@AndroidEntryPoint
class SlideableListItemLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), SlideableItem, SwapableListItem {

  private val content: BaseSlideableContent<SlideableListItemData> by lazy {
    findViewById<BaseSlideableContent<SlideableListItemData>>(R.id.list_item_content)
      ?: throw IllegalStateException("No content view found")
  }
  private val leftItem: View? by lazy { findViewById(R.id.list_item_left_item) }
  private val rightItem: View? by lazy { findViewById(R.id.list_item_right_item) }

  private var updateDisposable: Disposable? = null
  private var data: SlideableListItemData? = null

  private var remoteId: Int? = null
  private var itemType: ItemType? = null
  private var onInfoClick: () -> Unit = { }
  private var onIssueClick: () -> Unit = { }
  private var onTitleLongClick: () -> Unit = { }
  private var onItemClick: () -> Unit = { }

  override var locationCaption: String? = null

  @Inject
  lateinit var preferences: Preferences

  @Inject
  lateinit var createListItemUpdateEventDataUseCase: CreateListItemUpdateEventDataUseCase

  @Inject
  lateinit var schedulers: SuplaSchedulers

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val desiredHeight = resources.getDimensionPixelSize(R.dimen.channel_layout_height) * getScaleFactor()

    val heightMode = MeasureSpec.getMode(heightMeasureSpec)
    val heightSize = MeasureSpec.getSize(heightMeasureSpec)

    val height = when (heightMode) {
      MeasureSpec.EXACTLY -> heightSize
      MeasureSpec.AT_MOST -> min(desiredHeight.toInt(), heightSize)
      else -> desiredHeight.toInt()
    }

    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))

    updateContentView(data)
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    super.onLayout(changed, l, t, r, b)
    if (childCount !in 1..3) {
      throw IllegalStateException("SlideableLayout expects 1 to 3 views inside but found '$childCount'!")
    }

    content.elevation = 0f
    leftItem?.elevation = 0f
    rightItem?.elevation = 0f
  }

  override fun slide(position: Int) {
    val correctedPosition = when {
      (leftItem == null && rightItem == null) ||
        (leftItem == null && position > 0) ||
        (rightItem == null && position < 0) ||
        data?.online?.not() == true -> 0

      else -> position
    }

    content.layout(correctedPosition, content.top, content.width + correctedPosition, content.height)
    leftItem?.run { updateLeftItemPosition(this) }
    rightItem?.run { updateRightItemPosition(this) }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    content.onInfoClick = onInfoClick
    content.onIssueClick = onIssueClick
    content.onTitleLongClick = onTitleLongClick
    content.onItemClick = onItemClick

    val (itemType) = guardLet(itemType) { return }
    val (remoteId) = guardLet(remoteId) { return }

    updateDisposable =
      createListItemUpdateEventDataUseCase(itemType, remoteId)
        .delay(1, TimeUnit.SECONDS)
        .subscribeOn(schedulers.io)
        .observeOn(schedulers.ui)
        .subscribeBy(
          onNext = { data ->
            this.data = data
            updateContentView(data)
          }
        )
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    if (updateDisposable?.isDisposed == false) {
      updateDisposable?.dispose()
    }
  }

  fun bind(
    itemType: ItemType,
    remoteId: Int,
    locationCaption: String,
    data: SlideableListItemData,
    onInfoClick: () -> Unit,
    onIssueClick: () -> Unit,
    onTitleLongClick: () -> Unit,
    onItemClick: () -> Unit
  ) {
    this.itemType = itemType
    this.remoteId = remoteId
    this.locationCaption = locationCaption
    this.data = data
    this.onInfoClick = onInfoClick
    this.onIssueClick = onIssueClick
    this.onTitleLongClick = onTitleLongClick
    this.onItemClick = onItemClick
  }

  private fun updateLeftItemPosition(item: View) {
    val percentage = (content.left * 100 / item.width.toFloat()).let {
      when {
        it < 0 -> 0f
        it > 100 -> 100f
        else -> it
      }
    }
    item.rotationY = 90 - 90 * percentage / 100
    val left = (content.left / 2 - item.width / 2).let { if (it > 0) 0 else it }
    val right = (item.width + (content.left / 2 - item.width / 2)).let { if (it > item.width) item.width else it }
    item.layout(left, 0, right, item.height)
  }

  private fun updateRightItemPosition(item: View) {
    val percentage = (-content.left * 100 / item.width.toFloat()).let {
      when {
        it < 0 -> 0f
        it > 100 -> 100f
        else -> it
      }
    }
    item.rotationY = 90 * percentage / 100 - 90
    val left = if (content.left * -1 > item.width) {
      width - item.width
    } else {
      width + (content.left / 2 - item.width / 2)
    }

    item.layout(left, 0, left + item.width, item.height)
  }

  private fun getScaleFactor() =
    if (this::preferences.isInitialized) { // hack for previews in Android Studio
      preferences.channelHeight.div(100f)
    } else {
      0f
    }

  private fun updateContentView(data: SlideableListItemData?) {
    ifLet(data) { (data) -> content.update(data = data) }
  }
}
