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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.supla.android.R
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
) =
  SwitchButtonsLayout(
    modifier = Modifier.padding(horizontal = Distance.horizontal, vertical = Distance.vertical)
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
private fun SwitchButtonsLayout(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val defaultDistance = Distance.default
  val configuration = LocalWindowInfo.current
  val screenWidth = with(LocalDensity.current) { configuration.containerSize.width.dp.toPx().toInt() }
  val screenHeight = with(LocalDensity.current) { configuration.containerSize.height.dp.toPx().toInt() }

  Layout(modifier = modifier, content = content) { measurables, constraints ->
    val spacing = defaultDistance.toPx().toInt()
    val buttonMaxWidth = 300.dp.toPx().toInt()
    val possibleWidth = min(constraints.maxWidth, screenWidth)

    if (measurables.size == 1) {
      val buttonWidth = min(possibleWidth, buttonMaxWidth)
      val padding = possibleWidth.minus(buttonWidth).div(2)
      val modifiedConstraint = constraints.copy(minWidth = buttonWidth, maxWidth = buttonWidth)
      val placeable = measurables[0].measure(modifiedConstraint)

      layout(possibleWidth, placeable.height) {
        placeable.placeRelative(padding, 0)
      }
    } else if (measurables.size == 2) {
      val buttonWidth = min(possibleWidth.minus(spacing).div(2), buttonMaxWidth)
      val padding = possibleWidth.minus(buttonWidth.times(2).plus(spacing)).div(2)
      val modifiedConstraint = constraints.copy(minWidth = buttonWidth, maxWidth = buttonWidth)
      val placeable1 = measurables[0].measure(modifiedConstraint)
      val placeable2 = measurables[1].measure(modifiedConstraint)

      layout(possibleWidth, placeable1.height) {
        placeable1.placeRelative(padding, 0)
        placeable2.placeRelative(padding + buttonWidth + spacing, 0)
      }
    } else {
      layout(constraints.minWidth, constraints.minHeight) {}
    }
  }
}

@Composable
fun SwitchButton(
  text: String,
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
  ) {
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
          alignment = Alignment.Center,
          modifier = Modifier.size(dimensionResource(id = R.dimen.icon_default_size)),
        )
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = it,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
      )
    }
  }
}
