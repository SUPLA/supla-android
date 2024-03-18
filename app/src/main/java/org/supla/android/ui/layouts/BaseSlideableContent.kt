package org.supla.android.ui.layouts
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
import android.util.AttributeSet
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.supla.android.R
import org.supla.android.ui.lists.data.SlideableListItemData

abstract class BaseSlideableContent<T : SlideableListItemData> : BaseAbstractComposeView {

  constructor(context: Context) : super(context, null, 0)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
    loadAttributes(context, attrs)
  }

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    loadAttributes(context, attrs)
  }

  var onInfoClick: () -> Unit = { }
  var onIssueClick: () -> Unit = { }
  var onTitleLongClick: () -> Unit = { }
  var onItemClick: () -> Unit = { }

  protected var data: T? by mutableStateOf(null)
  protected var hasLeftButton: Boolean = false
  protected var hasRightButton: Boolean = false

  fun update(data: T) {
    this.data = data
    invalidate()
  }

  private fun loadAttributes(context: Context, attrs: AttributeSet?) {
    context.theme.obtainStyledAttributes(attrs, R.styleable.ThermostatListItemView, 0, 0).apply {
      try {
        hasLeftButton = getBoolean(R.styleable.ThermostatListItemView_hasLeftButton, false)
        hasRightButton = getBoolean(R.styleable.ThermostatListItemView_hasRightButton, false)
      } finally {
        recycle()
      }
    }
  }
}
