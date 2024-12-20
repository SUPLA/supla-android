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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.buttons.supla.controlbutton.ControlButtonIcon
import org.supla.android.ui.views.buttons.supla.controlbutton.ControlButtonScope

private val BUTTON_WIDTH = 94.dp
private val BUTTON_HEIGHT = 64.dp
private val CORNER_RADIUS = BUTTON_HEIGHT.div(2)

@Composable
fun LeftRightControlButton(
  modifier: Modifier = Modifier,
  disabled: Boolean = false,
  leftContent: @Composable BoxScope.(text: Color) -> Unit,
  rightContent: @Composable BoxScope.(text: Color) -> Unit,
  middleContent: (@Composable BoxScope.(text: Color) -> Unit)? = null,
  leftEventHandler: (ControlButtonScope.() -> ControlButtonScope.OnEventHandler)? = null,
  middleEventHandler: (ControlButtonScope.() -> ControlButtonScope.OnEventHandler)? = null,
  rightEventHandler: (ControlButtonScope.() -> ControlButtonScope.OnEventHandler)? = null
) {
  ControlButtonScope {
    val upHandler = leftEventHandler?.let { it() }
    val downHandler = rightEventHandler?.let { it() }
    val middleHandler = middleEventHandler?.let { it() }

    Row(
      modifier = modifier
    ) {
      LeftButton(
        disabled = disabled,
        content = leftContent,
        onClick = upHandler?.onClick(),
        onTouchDown = upHandler?.onTouchDown(),
        onTouchUp = upHandler?.onTouchUp(),
        modifier = Modifier.weight(1f)
      )

      middleContent?.let {
        MiddleButton(
          disabled = disabled,
          content = middleContent,
          onClick = middleHandler?.onClick(),
          onTouchDown = middleHandler?.onTouchDown(),
          onTouchUp = middleHandler?.onTouchUp()
        )
      }

      RightButton(
        disabled = disabled,
        content = rightContent,
        onClick = downHandler?.onClick(),
        onTouchDown = downHandler?.onTouchDown(),
        onTouchUp = downHandler?.onTouchUp(),
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
fun BoxScope.LeftControlIcon(textColor: Color) =
  ControlButtonIcon(
    iconRes = R.drawable.ic_arrow_right,
    textColor = textColor,
    rotate = 180f,
    modifier = Modifier.align(Alignment.Center)
  )

@Composable
fun BoxScope.RightControlIcon(textColor: Color) =
  ControlButtonIcon(
    iconRes = R.drawable.ic_arrow_right,
    textColor = textColor,
    modifier = Modifier.align(Alignment.Center)
  )

@Composable
private fun LeftButton(
  disabled: Boolean,
  content: @Composable BoxScope.(text: Color) -> Unit,
  onClick: (() -> Unit)?,
  onTouchDown: (() -> Unit)?,
  onTouchUp: (() -> Unit)?,
  modifier: Modifier = Modifier
) {
  SuplaButton(
    disabled = disabled,
    onClick = onClick ?: {},
    onTouchDown = onTouchDown ?: {},
    onTouchUp = onTouchUp ?: {},
    modifier = modifier,
    shape = SuplaButtonDefaults.startRoundedShape(radius = CORNER_RADIUS, minWidth = BUTTON_WIDTH),
  ) {
    content(it)
  }
}

@Composable
private fun MiddleButton(
  disabled: Boolean,
  content: @Composable BoxScope.(text: Color) -> Unit,
  onClick: (() -> Unit)?,
  onTouchDown: (() -> Unit)?,
  onTouchUp: (() -> Unit)?,
  modifier: Modifier = Modifier
) {
  SuplaButton(
    disabled = disabled,
    onClick = onClick ?: {},
    onTouchDown = onTouchDown ?: {},
    onTouchUp = onTouchUp ?: {},
    modifier = modifier,
    shape = SuplaButtonDefaults.notRoundedShape(minWidth = BUTTON_WIDTH, minHeight = BUTTON_HEIGHT),
  ) {
    content(it)
  }
}

@Composable
private fun RightButton(
  disabled: Boolean,
  content: @Composable BoxScope.(text: Color) -> Unit,
  onClick: (() -> Unit)?,
  onTouchDown: (() -> Unit)?,
  onTouchUp: (() -> Unit)?,
  modifier: Modifier = Modifier
) {
  SuplaButton(
    disabled = disabled,
    onClick = onClick ?: {},
    onTouchDown = onTouchDown ?: {},
    onTouchUp = onTouchUp ?: {},
    modifier = modifier,
    shape = SuplaButtonDefaults.endRoundedShape(radius = CORNER_RADIUS, minWidth = BUTTON_WIDTH),
  ) {
    content(it)
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
        .background(MaterialTheme.colorScheme.background)
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
