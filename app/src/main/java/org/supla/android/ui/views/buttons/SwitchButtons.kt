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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import org.supla.android.core.ui.theme.Distance
import org.supla.android.extensions.disabledOverlay
import org.supla.android.images.ImageId
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.buttons.supla.SuplaButton
import org.supla.android.ui.views.buttons.supla.SuplaButtonColors
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults
import kotlin.math.min

data class SwitchButtonState(
  val icon: ImageId?,
  val textRes: Int,
  val pressed: Boolean = false
)

@Composable
fun SwitchButtons(
  leftButton: SwitchButtonState?,
  rightButton: SwitchButtonState?,
  disabled: Boolean = false,
  leftButtonClick: () -> Unit = {},
  rightButtonClick: () -> Unit = {},
  leftColors: SuplaButtonColors = SuplaButtonDefaults.errorColors(contentDisabled = MaterialTheme.colorScheme.onSurface),
  rightColors: SuplaButtonColors = SuplaButtonDefaults.primaryColors(contentDisabled = MaterialTheme.colorScheme.onSurface)
) = SwitchButtons(
  leftButton = leftButton,
  rightButton = rightButton,
  disabled = disabled,
  leftButtonClick = leftButtonClick,
  rightButtonClick = rightButtonClick,
  leftColors = leftColors,
  rightColors = rightColors,
  modifier = Modifier.padding(horizontal = Distance.horizontal, vertical = Distance.vertical)
)

@Composable
fun SwitchButtons(
  leftButton: SwitchButtonState?,
  rightButton: SwitchButtonState?,
  disabled: Boolean = false,
  leftButtonClick: () -> Unit = {},
  rightButtonClick: () -> Unit = {},
  leftColors: SuplaButtonColors = SuplaButtonDefaults.errorColors(contentDisabled = MaterialTheme.colorScheme.onSurface),
  rightColors: SuplaButtonColors = SuplaButtonDefaults.primaryColors(contentDisabled = MaterialTheme.colorScheme.onSurface),
  modifier: Modifier
) =
  SwitchButtonsLayout(
    modifier = modifier
  ) {
    leftButton?.let {
      SwitchButton(
        icon = it.icon,
        text = stringResource(id = it.textRes),
        colors = leftColors,
        disabled = disabled,
        pressed = it.pressed,
        onClick = leftButtonClick,
        modifier = Modifier.widthIn(max = 120.dp)
      )
    }
    rightButton?.let {
      SwitchButton(
        icon = it.icon,
        text = stringResource(id = it.textRes),
        colors = rightColors,
        disabled = disabled,
        pressed = it.pressed,
        onClick = rightButtonClick,
        modifier = Modifier.widthIn(max = 120.dp)
      )
    }
  }

@Composable
fun SwitchButtonsLayout(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val defaultDistance = Distance.default
  val smallDistance = Distance.small
  val configuration = LocalWindowInfo.current
  val screenWidth = with(LocalDensity.current) { configuration.containerSize.width.dp.toPx().toInt() }

  Layout(modifier = modifier, content = content) { measurables, constraints ->
    val spacing = defaultDistance.toPx().toInt()
    val smallSpacing = smallDistance.toPx().toInt()
    val buttonMaxWidth = 300.dp.toPx().toInt()
    val possibleWidth = min(constraints.maxWidth, screenWidth)

    when (measurables.size) {
      1 -> layoutSingleButton(measurables, constraints, possibleWidth, buttonMaxWidth)
      2 -> layoutTwoButtons(measurables, constraints, possibleWidth, buttonMaxWidth, spacing)
      3 -> layoutThreeButtons(measurables, constraints, possibleWidth, buttonMaxWidth, smallSpacing)
      else -> layoutNoButtons(constraints)
    }
  }
}

private fun MeasureScope.layoutSingleButton(
  measurables: List<Measurable>,
  constraints: Constraints,
  possibleWidth: Int,
  buttonMaxWidth: Int
): MeasureResult {
  val buttonWidth = min(possibleWidth, buttonMaxWidth)
  val padding = possibleWidth.minus(buttonWidth).div(2)
  val modifiedConstraint = constraints.copy(minWidth = buttonWidth, maxWidth = buttonWidth)
  val placeable = measurables[0].measure(modifiedConstraint)

  return layout(possibleWidth, placeable.height) {
    placeable.placeRelative(padding, 0)
  }
}

private fun MeasureScope.layoutTwoButtons(
  measurables: List<Measurable>,
  constraints: Constraints,
  possibleWidth: Int,
  buttonMaxWidth: Int,
  spacing: Int
): MeasureResult {
  val buttonWidth = min(possibleWidth.minus(spacing).div(2), buttonMaxWidth)
  val padding = possibleWidth.minus(buttonWidth.times(2).plus(spacing)).div(2)
  val modifiedConstraint = constraints.copy(minWidth = buttonWidth, maxWidth = buttonWidth)
  val placeable1 = measurables[0].measure(modifiedConstraint)
  val placeable2 = measurables[1].measure(modifiedConstraint)

  return layout(possibleWidth, placeable1.height) {
    placeable1.placeRelative(padding, 0)
    placeable2.placeRelative(padding + buttonWidth + spacing, 0)
  }
}

private fun MeasureScope.layoutThreeButtons(
  measurables: List<Measurable>,
  constraints: Constraints,
  possibleWidth: Int,
  buttonMaxWidth: Int,
  spacing: Int
): MeasureResult {
  val availableButtonWidth = possibleWidth.minus(spacing.times(2)).coerceAtLeast(0)
  val middleMaxWidth = min(availableButtonWidth.div(3), buttonMaxWidth)
  val middleConstraint = constraints.copy(minWidth = 0, maxWidth = middleMaxWidth)
  val placeable2 = measurables[1].measure(middleConstraint)
  val buttonWidth = min(
    possibleWidth.minus(placeable2.width).minus(spacing.times(2)).div(2).coerceAtLeast(0),
    buttonMaxWidth
  )
  val sideConstraint = constraints.copy(minWidth = buttonWidth, maxWidth = buttonWidth)
  val placeable1 = measurables[0].measure(sideConstraint)
  val placeable3 = measurables[2].measure(sideConstraint)
  val contentWidth = buttonWidth.times(2).plus(placeable2.width).plus(spacing.times(2))
  val padding = possibleWidth.minus(contentWidth).div(2)
  val height = maxOf(placeable1.height, placeable2.height, placeable3.height)

  return layout(possibleWidth, height) {
    placeable1.placeRelative(padding, 0)
    placeable2.placeRelative(padding + buttonWidth + spacing, 0)
    placeable3.placeRelative(
      padding + buttonWidth + spacing + placeable2.width + spacing,
      0
    )
  }
}

private fun MeasureScope.layoutNoButtons(constraints: Constraints): MeasureResult =
  layout(constraints.minWidth, constraints.minHeight) {}

@Composable
fun SwitchIconButton(
  state: SwitchButtonState,
  modifier: Modifier = Modifier,
  disabled: Boolean = false,
  colors: SuplaButtonColors,
  onClick: () -> Unit
) = SwitchButton(
  text = null,
  icon = state.icon,
  modifier = modifier,
  disabled = disabled,
  pressed = state.pressed,
  colors = colors,
  onClick = onClick
)

@Composable
fun SwitchButton(
  text: String?,
  icon: ImageId?,
  modifier: Modifier = Modifier,
  disabled: Boolean = false,
  pressed: Boolean = false,
  colors: SuplaButtonColors,
  onClick: () -> Unit
) {
  val shape = SuplaButtonDefaults.allRoundedShape()
  SuplaButton(
    onClick = onClick,
    modifier = modifier.disabledOverlay(disabled, radius = shape.topEndRadius),
    disabled = disabled,
    active = pressed,
    colors = colors,
    shape = shape
  ) { color ->
    Row(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = Distance.small),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
    ) {
      icon?.let {
        Image(
          imageId = it,
          contentDescription = null,
          alignment = Alignment.Center
        )
      }
      text?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.labelLarge,
          color = color,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
        )
      }
    }
  }
}
