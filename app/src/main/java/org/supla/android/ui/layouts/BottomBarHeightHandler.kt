package org.supla.android.ui.layouts

import android.content.res.Resources
import androidx.constraintlayout.widget.ConstraintLayout
import org.supla.android.Preferences
import org.supla.android.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BottomBarHeightHandler @Inject constructor(
  private val preferences: Preferences
) {

  fun getLayoutParams(resources: Resources, visible: Boolean = preferences.isShowBottomMenu): ConstraintLayout.LayoutParams {
    val height = when {
      visible && preferences.isShowBottomLabel -> resources.getDimension(R.dimen.bottom_bar_height).toInt()
      visible -> resources.getDimension(R.dimen.bottom_bar_without_label_height).toInt()
      else -> 0
    }

    return ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, height).apply {
      bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
    }
  }
}
