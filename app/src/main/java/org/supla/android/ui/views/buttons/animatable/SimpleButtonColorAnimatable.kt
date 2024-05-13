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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.extensions.innerShadow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SimpleButtonColorAnimatable(
  modifier: Modifier = Modifier,
  height: Dp,
  padding: Dp,
  type: AnimatableButtonType,
  disabled: Boolean,
  backgroundColor: Color = colorResource(id = R.color.control_button_background),
  animationMode: AnimationMode = AnimationMode.Pressed,
  onClick: () -> Unit,
  onLongClick: (() -> Unit)? = null,
  content: @Composable BoxScope.(text: Color) -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }

  val colorDisabled = colorResource(id = R.color.disabled)
  val pressedColor = colorResource(id = type.pressedColor)
  val textColorRes = colorResource(id = type.textColor)

  ViewColorAnimatable(
    type = type,
    interactionSource = interactionSource,
    disabled = disabled
  ) { borderColor, outerShadowColor, innerShadowColor, textColor ->
    Box(
      modifier = modifier
        .height(height)
        .padding(all = padding)
        .border(
          width = 1.dp,
          color = if (animationMode.isActiveState()) pressedColor else borderColor,
          shape = RoundedCornerShape(size = height.minus(padding.times(2)))
        )
        .shadow(
          elevation = 4.dp,
          shape = CircleShape,
          ambientColor = if (animationMode.isActiveState()) pressedColor else outerShadowColor,
          spotColor = if (animationMode.isActiveState()) pressedColor else outerShadowColor
        )
        .innerShadow(
          color = if (animationMode.isActiveState()) colorDisabled else innerShadowColor,
          blur = 5.dp,
          cornersRadius = height.minus(padding.times(2)),
          offsetY = 4.dp
        )
        .background(color = backgroundColor)
        .combinedClickable(
          onClick = onClick,
          onLongClick = onLongClick,
          indication = null,
          interactionSource = interactionSource
        )
    ) {
      content(if (animationMode.isActiveState()) textColorRes else textColor)
    }
  }
}

fun Int.toAnimatableButtonType(): AnimatableButtonType {
  for (type in AnimatableButtonType.values()) {
    if (type.value == this) {
      return type
    }
  }

  throw IllegalStateException("No AnimatableButtonType for `$this`")
}
