package org.supla.android.ui.layouts

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import org.supla.android.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DurationTimerHelper @Inject constructor() {
  fun createTimerView(context: Context, scale: Float): TextView {
    val timerView = TextView(context)
    val textSize = context.resources.getDimension(R.dimen.default_text_size).let {
      if (scale < 1) {
        it.times(0.8f)
      } else {
        it
      }
    }
    timerView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    timerView.setTextColor(ResourcesCompat.getColor(context.resources, R.color.gray, null))
    timerView.gravity = Gravity.BOTTOM or Gravity.END

    return timerView
  }

  fun getTimerViewLayoutParams(context: Context, scale: Float): LayoutParams {
    val layoutParams = LayoutParams(
      context.resources.getDimension(R.dimen.channel_imgtext_width).toInt(),
      context.resources.getDimension(R.dimen.default_text_size).times(1.5).toInt().let {
        if (scale < 1) {
          it.times(0.8f).toInt()
        } else {
          it
        }
      }
    )
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)
    layoutParams.setMargins(
      0,
      context.resources.getDimension(R.dimen.distance_small).times(scale).toInt(),
      context.resources.getDimension(R.dimen.distance_small).toInt(),
      0
    )
    return layoutParams
  }

  @SuppressLint("DefaultLocale")
  fun formatMillis(leftTimeMillis: Long): String {
    // Plus 1 because we don't want to see 00:00:00 for one second, last second should be shown
    // as 00:00:01 and after that view should disappear.
    val leftTimeSecs = leftTimeMillis.div(1000).plus(1).toInt()
    val leftHours = leftTimeSecs / 3600
    val leftMinutes = (leftTimeSecs % 3600) / 60
    val leftSeconds = (leftTimeSecs % 60)
    return String.format("%02d:%02d:%02d", leftHours, leftMinutes, leftSeconds)
  }
}
