package org.supla.android.ui.views.list
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
import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.data.ValuesFormatter
import org.supla.android.databinding.IncListItemScaffoldBinding
import org.supla.android.extensions.backgroundTint
import org.supla.android.extensions.scaled
import org.supla.android.extensions.visibleIf
import org.supla.android.ui.layouts.BaseSlideableContent
import org.supla.android.ui.lists.data.SlideableListItemData
import java.util.Date
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
open class ListItemScaffoldView : ConstraintLayout, BaseSlideableContent<SlideableListItemData> {

  @Inject
  lateinit var preferences: Preferences

  @Inject
  lateinit var valuesFormatter: ValuesFormatter

  override var onInfoClick: () -> Unit = {}
  override var onIssueClick: () -> Unit = {}
  override var onTitleLongClick: () -> Unit = {}
  override var onItemClick: () -> Unit = {}
  override var data: SlideableListItemData? = null

  protected val scale: Float
    get() = preferences.channelHeight.div(100f)

  private lateinit var binding: IncListItemScaffoldBinding
  private var hasLeftButton: Boolean = false
  private var hasRightButton: Boolean = false
  private var countDownTimer: CountDownTimer? = null

  constructor(context: Context) : super(context, null, 0) {
    onViewInitialized()
  }

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
    loadAttributes(context, attrs)
    onViewInitialized()
  }

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    loadAttributes(context, attrs)
    onViewInitialized()
  }

  protected open fun onViewInitialized() {
    binding = IncListItemScaffoldBinding.inflate(LayoutInflater.from(context), this)

    // timer
    (binding.listItemTimer.layoutParams as LayoutParams).topMargin =
      resources.getDimension(R.dimen.distance_small).times(scale).roundToInt()
    scale(binding.listItemTimer, R.dimen.default_text_size, upperLimit = 1f)

    // dots
    binding.listItemLeadingDot.background = getDotBackground(hasLeftButton)
    binding.listItemTradingDot.background = getDotBackground(hasRightButton)

    // title
    (binding.listItemTitle.layoutParams as LayoutParams).bottomMargin =
      resources.getDimension(R.dimen.distance_small).times(scale).roundToInt()

    // icon (when overridden)
    listItemIcon()?.let {
      it.layoutParams.width =
        resources.getDimension(R.dimen.channel_img_width).scaled(scale).roundToInt()
      it.layoutParams.height =
        resources.getDimension(R.dimen.channel_img_height).scaled(scale).roundToInt()
    }

    // container
    listItemContainer()?.let {
      (it.layoutParams as LayoutParams).topMargin =
        resources.getDimension(R.dimen.distance_default).times(scale).roundToInt()
    }

    // value
    listItemValue()?.let {
      scale(it, R.dimen.channel_imgtext_size, lowerLimit = 1f)
    }

    binding.listItemInfoIcon.setOnClickListener { onInfoClick() }
    binding.listItemIssueIcon.setOnClickListener { onIssueClick }
    binding.root.setOnClickListener { onItemClick }
    binding.listItemTitle.setOnClickListener { onItemClick }
    binding.listItemTitle.setOnLongClickListener {
      onTitleLongClick()
      true
    }
  }

  override fun update(data: SlideableListItemData) {
    this.data = data

    binding.listItemTitle.text = data.titleProvider(context)

    binding.listItemLeadingDot.backgroundTint(if (data.online) R.color.primary else R.color.red)
    binding.listItemTradingDot.backgroundTint(if (data.online) R.color.primary else R.color.red)

    binding.listItemInfoIcon.visibleIf(data.online && preferences.isShowChannelInfo)
    binding.listItemIssueIcon.visibleIf(data.issueIconType != null)
    data.issueIconType?.let { binding.listItemIssueIcon.setImageResource(it.icon) }

    countDownTimer?.let {
      it.cancel()
      timerFinished()
    }

    data.estimatedTimerEndDate?.let {
      startTimer(it)
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()

    if (countDownTimer != null) {
      countDownTimer?.cancel()
      countDownTimer = null
    }
  }

  protected open fun timerStarted(durationMillis: Long) {
    setTimerText(durationMillis)
    binding.listItemTimer.visibility = VISIBLE
  }

  protected open fun timerFinished() {
    countDownTimer = null
    binding.listItemTimer.visibility = GONE
  }

  protected open fun listItemIcon(): AppCompatImageView? = null
  protected open fun listItemContainer(): ViewGroup? = null
  protected open fun listItemValue(): TextView? = null

  protected fun scale(view: TextView, @DimenRes fontSize: Int, lowerLimit: Float? = null, upperLimit: Float? = null) {
    val textSize = context.resources.getDimension(fontSize).scaled(scale, upperLimit, lowerLimit)
    view.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
  }

  private fun loadAttributes(context: Context, attrs: AttributeSet?) {
    context.theme.obtainStyledAttributes(attrs, R.styleable.ListItemScaffoldView, 0, 0).apply {
      try {
        hasLeftButton = getBoolean(R.styleable.ListItemScaffoldView_leftButtonEnabled, false)
        hasRightButton = getBoolean(R.styleable.ListItemScaffoldView_rightButtonEnabled, false)
      } finally {
        recycle()
      }
    }
  }

  private fun getDotBackground(hasButton: Boolean): Drawable? {
    return if (hasButton) {
      ResourcesCompat.getDrawable(resources, R.drawable.background_dot_solid, null)
    } else {
      ResourcesCompat.getDrawable(resources, R.drawable.background_dot_stroke, null)
    }
  }

  private fun startTimer(timerEndDate: Date) {
    val currentTime = System.currentTimeMillis()
    if (currentTime > timerEndDate.time) {
      return
    }
    val duration = timerEndDate.time - currentTime
    timerStarted(duration)
    countDownTimer = object : CountDownTimer(duration, 100) {
      override fun onTick(p0: Long) {
        setTimerText(timerEndDate.time - System.currentTimeMillis())
      }

      override fun onFinish() {
        timerFinished()
      }

    }.also { it.start() }
  }

  private fun setTimerText(durationMillis: Long) {
    val restTimeSec = durationMillis.div(1000).toInt()
    binding.listItemTimer.text = valuesFormatter.getTimerRestTime(restTimeSec)(context)
  }
}
