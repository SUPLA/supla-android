package org.supla.android.features.details.blindsdetail.ui
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

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import org.supla.android.R

data class WindowColors(
  val window: Color,
  val shadow: Color,
  val glassTop: Color,
  val glassBottom: Color,
  val slatBackground: Color,
  val slatBorder: Color,
  val markerBorder: Color,
  val markerBackground: Color
) {
  companion object {
    @Composable
    fun standard() =
      WindowColors(
        window = colorResource(id = R.color.blinds_window_color),
        shadow = colorResource(id = R.color.shadow_start),
        glassTop = colorResource(id = R.color.blinds_glass_top_color),
        glassBottom = colorResource(id = R.color.blinds_glass_bottom_color),
        slatBackground = colorResource(id = R.color.separator_light),
        slatBorder = colorResource(id = R.color.disabled),
        markerBorder = colorResource(id = R.color.on_background),
        markerBackground = colorResource(id = R.color.primary_variant)
      )

    @Composable
    fun offline() =
      WindowColors(
        window = colorResource(id = R.color.blinds_window_color),
        shadow = colorResource(id = R.color.shadow_start),
        glassTop = Color(0xffeffaff),
        glassBottom = Color(0xfff3fbff),
        slatBackground = Color(0xfff5f5f6),
        slatBorder = Color(0xffe9e9ea),
        markerBorder = colorResource(id = R.color.disabled),
        markerBackground = Color(0xffb3f1cb)
      )
  }
}
