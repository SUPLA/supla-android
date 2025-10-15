package org.supla.android.ui.views.buttons.supla
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

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
class SuplaButtonColors(
  val border: Color,
  val borderPressed: Color,
  val borderDisabled: Color,
  val content: Color,
  val contentPressed: Color,
  val contentDisabled: Color,
  val shadow: Color,
  val shadowPressed: Color
) {

  fun border(active: Boolean, disabled: Boolean) =
    when {
      disabled -> borderDisabled
      active -> borderPressed
      else -> border
    }

  fun content(active: Boolean, disabled: Boolean) =
    when {
      disabled -> contentDisabled
      active -> contentPressed
      else -> content
    }
}
