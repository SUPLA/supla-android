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

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.primaryVariant
import org.supla.android.extensions.buttonBackground
import org.supla.android.extensions.innerShadow

@Composable
fun SuplaButton(
  iconRes: Int,
  modifier: Modifier = Modifier,
  disabled: Boolean = false,
  pressed: Boolean = false,
  color: Color? = null,
  radius: Dp = dimensionResource(id = R.dimen.button_default_size).div(2),
  onClick: () -> Unit
) {
  SuplaButton(
    onClick = onClick,
    modifier = modifier,
    disabled = disabled,
    active = pressed,
    radius = radius
  ) {
    Image(
      painter = painterResource(id = iconRes),
      contentDescription = null,
      alignment = Alignment.Center,
      modifier = Modifier
        .size(dimensionResource(id = R.dimen.icon_default_size))
        .align(Alignment.Center),
      colorFilter = if (color != null && !disabled) ColorFilter.tint(color = color) else ColorFilter.tint(color = it),
    )
  }
}

@Composable
fun SuplaButton(
  text: String,
  modifier: Modifier = Modifier,
  disabled: Boolean = false,
  pressed: Boolean = false,
  radius: Dp = dimensionResource(id = R.dimen.button_default_size).div(2),
  onClick: () -> Unit
) {
  SuplaButton(
    onClick = onClick,
    modifier = modifier,
    disabled = disabled,
    active = pressed,
    radius = radius,
    minWidth = 124.dp
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      color = it,
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = 24.dp)
    )
  }
}

@Composable
fun SuplaButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  disabled: Boolean = false,
  active: Boolean = false,
  radius: Dp = dimensionResource(id = R.dimen.button_default_size).div(2),
  minWidth: Dp = radius.times(2),
  minHeight: Dp = radius.times(2),
  content: @Composable BoxScope.(foregroundColor: Color) -> Unit = {}
) {
  val outlineColor = MaterialTheme.colorScheme.outline
  val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
  val onBackgroundColor = MaterialTheme.colorScheme.onBackground
  val primaryVariantColor = MaterialTheme.colorScheme.primaryVariant
  val innerShadow = colorResource(id = R.color.supla_button_inner_shadow)

  val shape = RoundedCornerShape(size = radius)

  var pressed by remember { mutableStateOf(false) }
  var borderColor by remember(active, disabled) {
    mutableStateOf(
      when {
        active -> primaryVariantColor
        disabled -> outlineVariantColor
        else -> outlineColor
      }
    )
  }
  var shadowColor by remember(active) { mutableStateOf(if (active) primaryVariantColor else DefaultShadowColor) }
  val foregroundColor by remember(disabled) { mutableStateOf(if (disabled) outlineColor else onBackgroundColor) }

  Box(
    modifier = modifier
      .defaultMinSize(minWidth = minWidth, minHeight = minHeight)
      .border(
        width = 1.dp,
        color = borderColor,
        shape = shape
      )
      .shadow(
        elevation = 4.dp,
        shape = shape,
        ambientColor = shadowColor,
        spotColor = shadowColor
      )
      .buttonBackground(
        shape = shape,
        radius = radius
      )
      .innerShadow(
        color = innerShadow,
        blur = 6.dp,
        cornersRadius = radius,
        offsetY = 6.dp,
        active = { active || pressed }
      )
      .pointerInput(onClick) {
        detectTapGestures(
          onTap = { onClick() },
          onPress = {
            if (!disabled && !active) {
              pressed = true
              borderColor = primaryVariantColor
              shadowColor = primaryVariantColor

              tryAwaitRelease()
              pressed = false
              borderColor = outlineColor
              shadowColor = DefaultShadowColor
            }
          }
        )
      }
  ) {
    content(foregroundColor)
  }
}

@Composable
@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
private fun Preview() {
  SuplaTheme {
    Column(verticalArrangement = Arrangement.spacedBy(Distance.default), modifier = Modifier.padding(Distance.default)) {
      SuplaButton(onClick = {})
      SuplaButton(onClick = {}, disabled = true)
      SuplaButton(onClick = {}, active = true)
      SuplaButton(iconRes = R.drawable.ic_power_button, onClick = {})
      SuplaButton(iconRes = R.drawable.ic_power_button, onClick = {}, color = MaterialTheme.colorScheme.primary, disabled = true)
      SuplaButton(text = "Label", onClick = {})
      SuplaButton(text = "Label", onClick = {}, disabled = true)
      SuplaButton(text = "Label", onClick = {}, pressed = true)
      SuplaButton(text = "Very long label", onClick = {})
    }
  }
}
