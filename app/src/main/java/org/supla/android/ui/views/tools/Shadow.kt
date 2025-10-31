package org.supla.android.ui.views.tools
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import org.supla.android.R

@Composable
fun Shadow(modifier: Modifier = Modifier, orientation: ShadowOrientation = ShadowOrientation.STARTING_BOTTOM) {
  when (orientation) {
    ShadowOrientation.STARTING_TOP,
    ShadowOrientation.STARTING_BOTTOM ->
      Box(
        modifier = modifier
          .height(dimensionResource(id = R.dimen.custom_shadow_height))
          .fillMaxWidth()
          .background(
            brush = Brush.verticalGradient(colorStops = orientation.colors())
          )
      )

    ShadowOrientation.STARTING_LEFT,
    ShadowOrientation.STARTING_RIGHT ->
      Box(
        modifier = modifier
          .width(dimensionResource(id = R.dimen.custom_shadow_height))
          .fillMaxHeight()
          .background(
            brush = Brush.horizontalGradient(colorStops = orientation.colors())
          )
      )
  }
}

enum class ShadowOrientation(private val colors: Array<Pair<Float, Int>>) {
  STARTING_TOP(arrayOf(0f to R.color.shadow_start, 1f to R.color.shadow_end)),
  STARTING_BOTTOM(arrayOf(0f to R.color.shadow_end, 1f to R.color.shadow_start)),
  STARTING_LEFT(arrayOf(0f to R.color.shadow_start, 1f to R.color.shadow_end)),
  STARTING_RIGHT(arrayOf(0f to R.color.shadow_end, 1f to R.color.shadow_start));

  @Composable
  fun colors(): Array<Pair<Float, Color>> {
    return colors.map { item -> Pair(item.first, colorResource(id = item.second)) }.toTypedArray()
  }
}
