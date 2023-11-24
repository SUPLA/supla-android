package org.supla.android.ui.views
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.layouts.BaseSlideableContent
import org.supla.android.ui.lists.data.SlideableListItemData
import javax.inject.Inject

@AndroidEntryPoint
class EmptySlideableContent : BaseSlideableContent<SlideableListItemData.Thermostat> {

  constructor(context: Context) : super(context, null, 0)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
    loadAttributes(context, attrs)
  }

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    loadAttributes(context, attrs)
  }

  @Inject
  lateinit var preferences: Preferences

  var hasLeftButton: Boolean = false
  var hasRightButton: Boolean = false

  @Composable
  override fun Content() {
    SuplaTheme {
      Box(
        modifier = Modifier
          .background(Color.LightGray)
          .fillMaxWidth()
          .fillMaxHeight()
      )
    }
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
