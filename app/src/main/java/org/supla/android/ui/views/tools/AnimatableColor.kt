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

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.ui.graphics.Color

class AnimatableColor(val destinationColor: Color, val initialColor: Color) {
  private val animatable = Animatable(initialColor)
  val color: Color
    get() = animatable.value

  suspend fun reset() {
    animatable.snapTo(initialColor)
  }

  suspend fun animate(isPressed: Boolean) {
    if (isPressed) {
      animatable.snapTo(initialColor)
      animatable.animateTo(destinationColor, animationSpec = SpringSpec(stiffness = Spring.StiffnessHigh))
    } else {
      animatable.animateTo(initialColor, animationSpec = SpringSpec(stiffness = Spring.StiffnessHigh.times(2)))
    }
  }
}
