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

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.buttonBackground
import org.supla.android.extensions.innerShadow

@Composable
fun SuplaButton(
  iconRes: Int,
  modifier: Modifier = Modifier,
  disabled: Boolean = false,
  pressed: Boolean = false,
  colors: SuplaButtonColors = SuplaButtonDefaults.buttonColors(),
  shape: SuplaButtonShape = SuplaButtonDefaults.allRoundedShape(),
  onClick: () -> Unit
) {
  SuplaButton(
    onClick = onClick,
    modifier = modifier,
    disabled = disabled,
    active = pressed,
    shape = shape,
    colors = colors
  ) {
    Image(
      painter = painterResource(id = iconRes),
      contentDescription = null,
      alignment = Alignment.Center,
      modifier = Modifier
        .size(dimensionResource(id = R.dimen.icon_default_size))
        .align(Alignment.Center),
      colorFilter = ColorFilter.tint(color = it),
    )
  }
}

@Composable
fun SuplaButton(
  text: String,
  modifier: Modifier = Modifier,
  disabled: Boolean = false,
  pressed: Boolean = false,
  shape: SuplaButtonShape = SuplaButtonDefaults.allRoundedShape(minWidth = 124.dp),
  onClick: () -> Unit
) {
  SuplaButton(
    onClick = onClick,
    modifier = modifier,
    disabled = disabled,
    active = pressed,
    shape = shape,
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      color = it,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = Distance.small)
    )
  }
}

@Composable
fun SuplaButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
  onTouchDown: () -> Unit = {},
  onTouchUp: () -> Unit = {},
  disabled: Boolean = false,
  active: Boolean = false,
  shape: SuplaButtonShape = SuplaButtonDefaults.allRoundedShape(),
  colors: SuplaButtonColors = SuplaButtonDefaults.buttonColors(),
  content: @Composable BoxScope.(foregroundColor: Color) -> Unit = {}
) {
  val roundedCornerShape = shape.shape

  var pressed by remember { mutableStateOf(false) }

  var borderColor by remember(active, disabled) { mutableStateOf(colors.border(active, disabled)) }
  var shadowColor by remember(active) { mutableStateOf(if (active) colors.shadowPressed else colors.shadow) }
  var foregroundColor by remember(disabled) { mutableStateOf(colors.content(active, disabled)) }

  Box(
    modifier = modifier
      .defaultMinSize(minWidth = shape.minWidth, minHeight = shape.minHeight)
      .border(width = 1.dp, color = borderColor, shape = roundedCornerShape)
      .shadow(elevation = 4.dp, shape = roundedCornerShape, ambientColor = shadowColor, spotColor = shadowColor)
      .buttonBackground(shape = shape)
      .innerShadow(
        color = colorResource(id = R.color.supla_button_inner_shadow),
        blur = 6.dp,
        offsetY = 6.dp,
        active = { active || pressed },
        topLeftRadius = shape.topStartRadius,
        topRightRadius = shape.topEndRadius,
        bottomLeftRadius = shape.bottomStartRadius,
        bottomRightRadius = shape.bottomEndRadius
      )
      .pointerInput(onClick, disabled, active) {
        detectTapGestures(
          onTap = {
            if (!disabled) {
              onClick()
            }
          },
          onPress = {
            if (!disabled && !active) {
              onTouchDown()
              pressed = true
              borderColor = colors.borderPressed
              foregroundColor = colors.contentPressed
              shadowColor = colors.shadowPressed

              tryAwaitRelease()
              onTouchUp()
              pressed = false
              borderColor = colors.border
              foregroundColor = colors.content
              shadowColor = colors.shadow
            }
          }
        )
      }
  ) {
    content(foregroundColor)
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview(name = "Light mode", showBackground = true, widthDp = 220)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 220)
private fun Preview() {
  SuplaTheme {
    FlowRow(
      verticalArrangement = Arrangement.spacedBy(Distance.small),
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
      modifier = Modifier.padding(Distance.default)
    ) {
      SuplaButton(onClick = {})
      SuplaButton(onClick = {}, disabled = true)
      SuplaButton(onClick = {}, active = true)
      SuplaButton(iconRes = R.drawable.ic_power_button, onClick = {})
      SuplaButton(
        iconRes = R.drawable.ic_power_button,
        onClick = {},
        colors = SuplaButtonDefaults.buttonColors(content = MaterialTheme.colorScheme.primary)
      )
      SuplaButton(
        iconRes = R.drawable.ic_power_button,
        onClick = {},
        colors = SuplaButtonDefaults.buttonColors(content = MaterialTheme.colorScheme.primary),
        disabled = true
      )
      SuplaButton(
        iconRes = R.drawable.ic_power_button,
        onClick = {},
        shape = SuplaButtonDefaults.allRoundedShape(minHeight = 100.dp)
      )
      SuplaButton(
        iconRes = R.drawable.ic_power_button,
        onClick = {},
        shape = SuplaButtonDefaults.topRoundedShape(minHeight = 100.dp)
      )
      SuplaButton(
        iconRes = R.drawable.ic_power_button,
        onClick = {},
        shape = SuplaButtonDefaults.topRoundedShape(minHeight = 100.dp),
        pressed = true
      )
      SuplaButton(text = "Label", onClick = {})
      SuplaButton(text = "Label", onClick = {}, disabled = true)
      SuplaButton(text = "Label", onClick = {}, pressed = true)
      SuplaButton(text = "Very long label", onClick = {})
    }
  }
}
