package org.supla.android.ui.views.buttons.animatable
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

import androidx.annotation.ColorRes
import org.supla.android.R

enum class AnimatableButtonType(
  val value: Int,
  @ColorRes val textColor: Int,
  @ColorRes val pressedColor: Int,
  @ColorRes val inactiveColor: Int
) {
  POSITIVE(0, R.color.on_background, R.color.supla, R.color.on_background),
  NEGATIVE(1, R.color.error, R.color.error, R.color.on_background),
  BLUE(3, R.color.blue, R.color.blue, R.color.on_background),
  NEUTRAL(4, R.color.black, R.color.black, R.color.black)
}
