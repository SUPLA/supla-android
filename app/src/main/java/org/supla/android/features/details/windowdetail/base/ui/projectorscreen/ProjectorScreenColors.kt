package org.supla.android.features.details.windowdetail.base.ui.projectorscreen
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

data class ProjectorScreenColors(
  val screen: Color,
  val shadow: Color,
  val bottomRect: Color,
  val topRect: Color,
  val disabledOverlay: Color
) {
  companion object {
    @Composable
    fun standard() =
      ProjectorScreenColors(
        screen = colorResource(id = R.color.roller_shutter_window_color),
        shadow = colorResource(id = R.color.shadow_start),
        bottomRect = colorResource(id = R.color.roller_shutter_slat_background),
        topRect = colorResource(id = R.color.on_surface_variant),
        disabledOverlay = colorResource(id = R.color.roller_shutter_disabled_overlay)
      )
  }
}
