package org.supla.android.ui.views.buttons.animatable

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.res.colorResource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.ui.views.tools.AnimatableColor

private val transparentColor = Color(0x00FFFFFF)

@Composable
fun ViewColorAnimatable(
  type: AnimatableButtonType,
  interactionSource: MutableInteractionSource,
  disabled: Boolean,
  animationMode: AnimationMode = AnimationMode.Pressed,
  content: @Composable (borderColor: Color, outerShadowColor: Color, innerShadowColor: Color, textColor: Color) -> Unit
) {
  val onSurfaceColor = MaterialTheme.colors.onSurface
  val colorDisabled = colorResource(id = R.color.disabled)
  val pressedColor = colorResource(id = type.pressedColor)
  val textColorRes = colorResource(id = type.textColor)

  val borderColor = remember { AnimatableColor(pressedColor, colorDisabled) }
  val outerShadowColor = remember { AnimatableColor(pressedColor, DefaultShadowColor) }
  val innerShadowColor = remember { AnimatableColor(colorDisabled, transparentColor) }
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

  content(
    borderColor.color,
    outerShadowColor.color,
    innerShadowColor.color,
    if (animationMode.isActiveState()) textColorRes else textColor.color
  )
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
