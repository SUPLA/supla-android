package org.supla.android.ui.views.list.components
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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaComponentPreview

sealed interface RectColors {
  val top: Color
    @Composable get
  val bottom: Color
    @Composable get

  data object Online : RectColors {
    override val top: Color
      @Composable
      get() = MaterialTheme.colorScheme.error
    override val bottom: Color
      @Composable
      get() = MaterialTheme.colorScheme.primary
  }

  data object Active : RectColors {
    override val top: Color
      @Composable
      get() = MaterialTheme.colorScheme.surface
    override val bottom: Color
      @Composable
      get() = MaterialTheme.colorScheme.primary
  }
}

@Composable
fun ListItemRect(
  percentage: Float,
  colors: RectColors,
  modifier: Modifier = Modifier,
  paddingValues: PaddingValues = PaddingValues(0.dp)
) {
  val radius = 3.dp
  val size = DpSize(width = radius * 2, height = 25.dp)
  val outerShape = RoundedCornerShape(radius)
  val borderWidth = 0.5.dp
  if (percentage == 0f || percentage == 1f) {
    val color = if (percentage == 0f) colors.top else colors.bottom
    Box(
      modifier = modifier
        .padding(paddingValues = paddingValues)
        .size(size)
        .border(width = borderWidth, color = MaterialTheme.colorScheme.onBackground, shape = outerShape)
        .background(color = color, shape = outerShape)
    )
  } else {
    Column(
      modifier = Modifier
        .padding(paddingValues = paddingValues)
        .border(width = borderWidth, color = MaterialTheme.colorScheme.onBackground, shape = outerShape)
    ) {
      val topShape = RoundedCornerShape(topStart = radius, topEnd = radius)
      val topHeight = size.height.times(1f - percentage)

      Box(
        modifier = modifier
          .width(size.width)
          .height(topHeight)
          .background(color = colors.top, shape = topShape)
      )

      val bottomShape = RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
      val bottomHeight = size.height.times(percentage)

      Box(
        modifier = modifier
          .width(size.width)
          .height(bottomHeight)
          .background(color = colors.bottom, shape = bottomShape)
      )
    }
  }
}

@Composable
@SuplaComponentPreview
private fun Preview() {
  SuplaTheme {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(10.dp)) {
      ListItemRect(percentage = 0f, colors = RectColors.Active)
      ListItemRect(percentage = 0.25f, colors = RectColors.Active)
      ListItemRect(percentage = 1f, colors = RectColors.Active)
      ListItemRect(percentage = 0f, colors = RectColors.Online)
      ListItemRect(percentage = 0.25f, colors = RectColors.Online)
      ListItemRect(percentage = 1f, colors = RectColors.Online)
    }
  }
}
