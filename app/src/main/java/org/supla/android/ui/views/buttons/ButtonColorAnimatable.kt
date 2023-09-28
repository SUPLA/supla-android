package org.supla.android.ui.views.buttons
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.extensions.innerShadow
import org.supla.android.ui.views.tools.AnimatableColor

private val transparentColor = Color(0x00FFFFFF)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ButtonColorAnimatable(
  modifier: Modifier = Modifier,
  height: Dp,
  padding: Dp,
  type: AnimatableButtonType,
  disabled: Boolean,
  backgroundColor: Color = MaterialTheme.colors.surface,
  animationMode: AnimationMode = AnimationMode.Pressed,
  onClick: () -> Unit,
  onLongClick: () -> Unit = { },
  content: @Composable BoxScope.(text: Color) -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }

  val onSurfaceColor = MaterialTheme.colors.onSurface
  val colorDisabled = colorResource(id = R.color.disabled)
  val pressedColor = colorResource(id = type.pressedColor)

  val borderColor = remember { AnimatableColor(pressedColor, colorDisabled) }
  val outerShadowColor = remember { AnimatableColor(pressedColor, DefaultShadowColor) }
  val innerShadowColor = remember { AnimatableColor(colorDisabled, transparentColor) }
  val textColorRes = colorResource(id = type.textColor)
  val textColor = remember { AnimatableColor(textColorRes, onSurfaceColor) }

  if (animationMode is AnimationMode.Toggle) {
    if (animationMode.isActiveState()) {
      LaunchAnimationManualPressing(interactionSource, borderColor, outerShadowColor, innerShadowColor, textColor)
    } else {
      LaunchAnimationManualReleasing(interactionSource, borderColor, outerShadowColor, innerShadowColor, textColor)
    }
  } else if (disabled.not()) {
    LaunchPressAnimation(interactionSource, borderColor, outerShadowColor, innerShadowColor, textColor)
  }

  Box(
    modifier = modifier
      .height(height)
      .padding(all = padding)
      .border(
        width = 1.dp,
        color = if (animationMode.isActiveState()) pressedColor else borderColor.color,
        shape = RoundedCornerShape(size = height.minus(padding.times(2)))
      )
      .shadow(
        elevation = 4.dp,
        shape = CircleShape,
        ambientColor = if (animationMode.isActiveState()) pressedColor else outerShadowColor.color,
        spotColor = if (animationMode.isActiveState()) pressedColor else outerShadowColor.color
      )
      .innerShadow(
        color = if (animationMode.isActiveState()) colorDisabled else innerShadowColor.color,
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
    content(if (animationMode.isActiveState()) textColorRes else textColor.color)
  }
}

@Composable
private fun LaunchAnimationManualPressing(interactionSource: MutableInteractionSource, vararg colors: AnimatableColor) {
  LaunchedEffect(interactionSource) {
    launch {
      interactionSource.interactions.collect { interaction ->
        if (interaction is PressInteraction.Press) {
          colors.map { async { it.animate(true) } }.awaitAll()
        }
      }
    }
  }
}

@Composable
private fun LaunchAnimationManualReleasing(interactionSource: MutableInteractionSource, vararg colors: AnimatableColor) {
  LaunchedEffect(interactionSource) {
    launch {
      colors.forEach { it.reset() }
      interactionSource.interactions.collect { interaction ->
        if (interaction is PressInteraction.Press) {
          colors.map { async { it.animate(false) } }.awaitAll()
        }
      }
    }
  }
}

@Composable
private fun LaunchPressAnimation(interactionSource: MutableInteractionSource, vararg colors: AnimatableColor) {
  LaunchedEffect(interactionSource) {
    launch {
      interactionSource.interactions.collect { interaction ->
        when (interaction) {
          is PressInteraction.Press -> {
            colors.map { async { it.animate(true) } }.awaitAll()
          }
          is PressInteraction.Release,
          is PressInteraction.Cancel -> {
            colors.map { async { it.animate(false) } }.awaitAll()
          }
        }
      }
    }
  }
}

enum class AnimatableButtonType(val value: Int, @ColorRes val textColor: Int, @ColorRes val pressedColor: Int) {
  POSITIVE(0, R.color.primary, R.color.primary_variant),
  NEGATIVE(1, R.color.red_alert, R.color.red_alert),
  BLUE(3, R.color.blue, R.color.blue),
  NEUTRAL(4, R.color.on_background, R.color.on_background)
}

fun Int.toAnimatableButtonType(): AnimatableButtonType {
  for (type in AnimatableButtonType.values()) {
    if (type.value == this) {
      return type
    }
  }

  throw IllegalStateException("No AnimatableButtonType for `$this`")
}

sealed interface AnimationMode {

  fun isActiveState(): Boolean

  enum class State {
    ACTIVE, CLEAR
  }

  object Pressed : AnimationMode {
    override fun isActiveState() = false
  }

  data class Toggle(val state: State) : AnimationMode {
    constructor(active: Boolean) : this(if (active) State.ACTIVE else State.CLEAR)

    override fun isActiveState() = state == State.ACTIVE
  }

  data class Stated(val state: State) : AnimationMode {
    constructor(active: Boolean) : this(if (active) State.ACTIVE else State.CLEAR)

    override fun isActiveState() = state == State.ACTIVE
  }
}
