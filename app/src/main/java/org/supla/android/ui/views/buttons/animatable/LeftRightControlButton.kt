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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.disabledOverlay
import org.supla.android.extensions.innerShadow
import org.supla.android.ui.views.buttons.animatable.controlbutton.ControlButtonIcon
import org.supla.android.ui.views.buttons.animatable.controlbutton.ControlButtonScope

private val BUTTON_WIDTH = 94.dp
private val BUTTON_HEIGHT = 64.dp
private val CORNER_RADIUS = BUTTON_HEIGHT.div(2)

@Composable
fun LeftRightControlButton(
  modifier: Modifier = Modifier,
  disabled: Boolean = false,
  animationMode: AnimationMode = AnimationMode.Pressed,
  leftContent: @Composable BoxScope.(text: Color) -> Unit,
  rightContent: @Composable BoxScope.(text: Color) -> Unit,
  middleContent: (@Composable BoxScope.(text: Color) -> Unit)? = null,
  leftEventHandler: (ControlButtonScope.() -> ControlButtonScope.OnEventHandler)? = null,
  middleEventHandler: (ControlButtonScope.() -> ControlButtonScope.OnEventHandler)? = null,
  rightEventHandler: (ControlButtonScope.() -> ControlButtonScope.OnEventHandler)? = null
) {
  val type = AnimatableButtonType.POSITIVE

  ControlButtonScope {
    val upHandler = leftEventHandler?.let { it() }
    val downHandler = rightEventHandler?.let { it() }
    val middleHandler = middleEventHandler?.let { it() }

    Row(
      modifier = modifier
    ) {
      LeftButton(
        disabled = disabled,
        animationMode = animationMode,
        type = type,
        content = leftContent,
        onClick = upHandler?.onClick(),
        onTouchDown = upHandler?.onTouchDown(),
        onTouchUp = upHandler?.onTouchUp(),
        modifier = Modifier.weight(1f)
      )

      middleContent?.let {
        MiddleButton(
          disabled = disabled,
          animationMode = animationMode,
          type = type,
          content = middleContent,
          onClick = middleHandler?.onClick(),
          onTouchDown = middleHandler?.onTouchDown(),
          onTouchUp = middleHandler?.onTouchUp()
        )
      }

      RightButton(
        disabled = disabled,
        animationMode = animationMode,
        type = type,
        content = rightContent,
        onClick = downHandler?.onClick(),
        onTouchDown = downHandler?.onTouchDown(),
        onTouchUp = downHandler?.onTouchUp(),
        modifier = Modifier.weight(1f)
      )
    }
  }
}

context(BoxScope)
@Composable
fun LeftControlIcon(textColor: Color) =
  ControlButtonIcon(
    iconRes = R.drawable.ic_arrow_right,
    textColor = textColor,
    rotate = 180f,
    modifier = Modifier.align(Alignment.Center)
  )

context(BoxScope)
@Composable
fun RightControlIcon(textColor: Color) =
  ControlButtonIcon(
    iconRes = R.drawable.ic_arrow_right,
    textColor = textColor,
    modifier = Modifier.align(Alignment.Center)
  )

@Composable
private fun LeftButton(
  disabled: Boolean,
  animationMode: AnimationMode,
  type: AnimatableButtonType,
  content: @Composable BoxScope.(text: Color) -> Unit,
  onClick: (() -> Unit)?,
  onTouchDown: (() -> Unit)?,
  onTouchUp: (() -> Unit)?,
  modifier: Modifier = Modifier
) {
  ControlButton(
    disabled = disabled,
    animationMode = animationMode,
    type = type,
    content = content,
    onClick = onClick,
    onTouchDown = onTouchDown,
    onTouchUp = onTouchUp,
    modifier = modifier,
    topStartCornerRadius = CORNER_RADIUS,
    bottomStartCornerRadius = CORNER_RADIUS
  )
}

@Composable
private fun MiddleButton(
  disabled: Boolean,
  animationMode: AnimationMode,
  type: AnimatableButtonType,
  content: @Composable BoxScope.(text: Color) -> Unit,
  onClick: (() -> Unit)?,
  onTouchDown: (() -> Unit)?,
  onTouchUp: (() -> Unit)?,
  modifier: Modifier = Modifier
) {
  ControlButton(
    disabled = disabled,
    animationMode = animationMode,
    type = type,
    content = content,
    onClick = onClick,
    onTouchDown = onTouchDown,
    onTouchUp = onTouchUp,
    modifier = modifier
  )
}

@Composable
private fun RightButton(
  disabled: Boolean,
  animationMode: AnimationMode,
  type: AnimatableButtonType,
  content: @Composable BoxScope.(text: Color) -> Unit,
  onClick: (() -> Unit)?,
  onTouchDown: (() -> Unit)?,
  onTouchUp: (() -> Unit)?,
  modifier: Modifier = Modifier
) {
  ControlButton(
    disabled = disabled,
    animationMode = animationMode,
    type = type,
    content = content,
    onClick = onClick,
    onTouchDown = onTouchDown,
    onTouchUp = onTouchUp,
    modifier = modifier,
    topEndCornerRadius = CORNER_RADIUS,
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
  modifier: Modifier = Modifier,
  topStartCornerRadius: Dp = 0.dp,
  topEndCornerRadius: Dp = 0.dp,
  bottomStartCornerRadius: Dp = 0.dp,
  bottomEndCornerRadius: Dp = 0.dp
) {
  val pressedColor = colorResource(id = type.pressedColor)
  val colorDisabled = colorResource(id = R.color.disabled)

  val upInteractionSource = remember { MutableInteractionSource() }
  LaunchedEffect(upInteractionSource, disabled) {
    launch {
      upInteractionSource.interactions.collect { interaction ->
        if (interaction is PressInteraction.Press) {
          if (!disabled) {
            onTouchDown?.let { it() }
          }
        } else {
          if (!disabled) {
            onTouchUp?.let { it() }
          }
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
      modifier = modifier
        .defaultMinSize(minWidth = BUTTON_WIDTH)
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
        .background(colorResource(id = R.color.control_button_background), shape = shape)
        .clickable(
          interactionSource = upInteractionSource,
          indication = null,
          onClick = {
            if (!disabled) {
              onClick?.let { it() }
            }
          }
        )
        .disabledOverlay(disabled)

    ) {
      content(textColor)
    }
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Box(
      modifier = Modifier
        .width(300.dp)
        .height(300.dp)
        .background(MaterialTheme.colors.background)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LeftRightControlButton(
          leftContent = { LeftControlIcon(textColor = it) },
          rightContent = { RightControlIcon(textColor = it) }
        )
        LeftRightControlButton(
          middleContent = {
            ControlButtonIcon(iconRes = R.drawable.ic_stop, textColor = it, rotate = 180f, modifier = Modifier.align(Alignment.Center))
          },
          leftContent = { LeftControlIcon(textColor = it) },
          rightContent = { RightControlIcon(textColor = it) }
        )
        LeftRightControlButton(
          disabled = true,
          middleContent = {
            ControlButtonIcon(iconRes = R.drawable.ic_stop, textColor = it, rotate = 180f, modifier = Modifier.align(Alignment.Center))
          },
          leftContent = { LeftControlIcon(textColor = it) },
          rightContent = { RightControlIcon(textColor = it) }
        )
      }
    }
  }
}
