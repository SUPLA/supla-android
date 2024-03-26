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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.innerShadow
import org.supla.android.ui.views.buttons.animatable.upanddown.UpDownControlButtonScope

val TOTAL_HEIGHT = 188.dp
private val BUTTON_WIDTH = 64.dp
private val BUTTON_HEIGHT = TOTAL_HEIGHT.div(2)
private val CORNER_RADIUS = BUTTON_WIDTH.div(2)
private val DISABLED_OVERLAY = Color(0xDDFFFFFF)

@Composable
fun UpDownControlButton(
  disabled: Boolean = false,
  animationMode: AnimationMode = AnimationMode.Pressed,
  upContent: @Composable BoxScope.(text: Color) -> Unit,
  downContent: @Composable BoxScope.(text: Color) -> Unit,
  upEventHandler: (UpDownControlButtonScope.() -> UpDownControlButtonScope.OnEventHandler)? = null,
  downEventHandler: (UpDownControlButtonScope.() -> UpDownControlButtonScope.OnEventHandler)? = null
) {
  val type = AnimatableButtonType.POSITIVE

  UpDownControlButtonScope {
    val upHandler = upEventHandler?.let { it() }
    val downHandler = downEventHandler?.let { it() }

    Column(
      modifier = Modifier
    ) {
      UpButton(
        disabled = disabled,
        animationMode = animationMode,
        type = type,
        content = upContent,
        onClick = upHandler?.onClick(),
        onTouchDown = upHandler?.onTouchDown(),
        onTouchUp = upHandler?.onTouchUp()
      )

      DownButton(
        disabled = disabled,
        animationMode = animationMode,
        type = type,
        content = downContent,
        onClick = downHandler?.onClick(),
        onTouchDown = downHandler?.onTouchDown(),
        onTouchUp = downHandler?.onTouchUp()
      )
    }
  }
}

context(BoxScope)
@Composable
fun UpControlIcon(textColor: Color) =
  ControlButtonIcon(
    iconRes = R.drawable.ic_arrow_right,
    textColor = textColor,
    rotate = 270f
  )

context (BoxScope)
@Composable
fun DownControlIcon(textColor: Color) =
  ControlButtonIcon(
    iconRes = R.drawable.ic_arrow_right,
    textColor = textColor,
    rotate = 90f
  )

context (BoxScope)
@Composable
fun ControlButtonIcon(
  iconRes: Int,
  textColor: Color = MaterialTheme.colors.onSurface,
  rotate: Float = 0f
) = Icon(
  painter = painterResource(id = iconRes),
  contentDescription = null,
  tint = textColor,
  modifier = Modifier
    .align(Alignment.Center)
    .rotate(rotate)
)

@Composable
private fun UpButton(
  disabled: Boolean,
  animationMode: AnimationMode,
  type: AnimatableButtonType,
  content: @Composable BoxScope.(text: Color) -> Unit,
  onClick: (() -> Unit)?,
  onTouchDown: (() -> Unit)?,
  onTouchUp: (() -> Unit)?
) {
  ControlButton(
    disabled = disabled,
    animationMode = animationMode,
    type = type,
    content = content,
    onClick = onClick,
    onTouchDown = onTouchDown,
    onTouchUp = onTouchUp,
    topStartCornerRadius = CORNER_RADIUS,
    topEndCornerRadius = CORNER_RADIUS
  )
}

@Composable
private fun DownButton(
  disabled: Boolean,
  animationMode: AnimationMode,
  type: AnimatableButtonType,
  content: @Composable BoxScope.(text: Color) -> Unit,
  onClick: (() -> Unit)?,
  onTouchDown: (() -> Unit)?,
  onTouchUp: (() -> Unit)?
) {
  ControlButton(
    disabled = disabled,
    animationMode = animationMode,
    type = type,
    content = content,
    onClick = onClick,
    onTouchDown = onTouchDown,
    onTouchUp = onTouchUp,
    bottomStartCornerRadius = CORNER_RADIUS,
    bottomEndCornerRadius = CORNER_RADIUS
  )
}

@Composable
private fun ControlButton(
  disabled: Boolean,
  animationMode: AnimationMode,
  type: AnimatableButtonType,
  content: @Composable BoxScope.(text: Color) -> Unit,
  onClick: (() -> Unit)?,
  onTouchDown: (() -> Unit)?,
  onTouchUp: (() -> Unit)?,
  topStartCornerRadius: Dp = 0.dp,
  topEndCornerRadius: Dp = 0.dp,
  bottomStartCornerRadius: Dp = 0.dp,
  bottomEndCornerRadius: Dp = 0.dp
) {
  val pressedColor = colorResource(id = type.pressedColor)
  val colorDisabled = colorResource(id = R.color.disabled)

  val upInteractionSource = remember { MutableInteractionSource() }

  LaunchedEffect(upInteractionSource) {
    launch {
      upInteractionSource.interactions.collect { interaction ->
        if (interaction is PressInteraction.Press) {
          onTouchDown?.let { it() }
        } else {
          onTouchUp?.let { it() }
        }
      }
    }
  }

  ViewColorAnimatable(
    type = type,
    interactionSource = upInteractionSource,
    disabled = disabled
  ) { borderColor, outerShadowColor, innerShadowColor, textColor ->

    val shape = RoundedCornerShape(
      topStart = topStartCornerRadius,
      topEnd = topEndCornerRadius,
      bottomStart = bottomStartCornerRadius,
      bottomEnd = bottomEndCornerRadius
    )
    Box(
      modifier = Modifier
        .width(BUTTON_WIDTH)
        .height(BUTTON_HEIGHT)
        .border(
          width = 1.dp,
          color = if (animationMode.isActiveState()) pressedColor else borderColor,
          shape = shape
        )
        .shadow(
          elevation = 4.dp,
          shape = shape,
          ambientColor = if (animationMode.isActiveState()) pressedColor else outerShadowColor,
          spotColor = if (animationMode.isActiveState()) pressedColor else outerShadowColor
        )
        .innerShadow(
          color = if (animationMode.isActiveState()) colorDisabled else innerShadowColor,
          blur = 5.dp,
          offsetY = 4.dp,
          topLeftRadius = topStartCornerRadius,
          topRightRadius = topEndCornerRadius,
          bottomLeftRadius = bottomStartCornerRadius,
          bottomRightRadius = bottomEndCornerRadius
        )
        .background(MaterialTheme.colors.surface, shape = shape)
        .clickable(
          interactionSource = upInteractionSource,
          indication = null,
          onClick = {
            if (!disabled) {
              onClick?.let { it() }
            }
          }
        )

    ) {
      content(textColor)

      if (disabled) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(color = DISABLED_OVERLAY, shape = shape)
        )
      }
    }
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Box(
      modifier = Modifier
        .width(200.dp)
        .height(300.dp)
        .background(MaterialTheme.colors.background)
    ) {
      UpDownControlButton(
        upContent = { UpControlIcon(textColor = it) },
        downContent = { DownControlIcon(textColor = it) }
      )
    }
  }
}
