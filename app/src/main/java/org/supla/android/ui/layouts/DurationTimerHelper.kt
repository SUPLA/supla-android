package org.supla.android.ui.layouts

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.widget.RelativeLayout
import android.widget.TextView
import org.supla.android.R
import org.supla.android.SuplaApp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DurationTimerHelper @Inject constructor() {
  fun createTimerView(context: Context): TextView {
    val timerView = TextView(context)
    timerView.typeface = SuplaApp.getApp().typefaceQuicksandRegular
    timerView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.default_text_size))
    timerView.setTextColor(context.resources.getColor(R.color.gray))
    timerView.gravity = Gravity.BOTTOM or Gravity.END

    return timerView
  }

  fun getTimerViewLayoutParams(context: Context, aboveId: Int, rightId: Int): RelativeLayout.LayoutParams {
    val layoutParams: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
      context.resources.getDimension(R.dimen.channel_imgtext_width).toInt(),
      context.resources.getDimension(R.dimen.default_text_size).times(1.5).toInt()
    )
    layoutParams.addRule(RelativeLayout.ABOVE, aboveId)
    layoutParams.addRule(RelativeLayout.ALIGN_RIGHT, rightId)
    layoutParams.setMargins(0, 0, 0, context.resources.getDimension(R.dimen.form_element_spacing).toInt())
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
