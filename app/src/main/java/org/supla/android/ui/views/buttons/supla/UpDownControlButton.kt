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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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

val TOTAL_HEIGHT = 188.dp
private val BUTTON_WIDTH = 64.dp
private val BUTTON_HEIGHT = TOTAL_HEIGHT.div(2)
private val CORNER_RADIUS = BUTTON_WIDTH.div(2)
private val MIDDLE_BUTTON_HEIGHT = 64.dp

@Composable
fun UpDownControlButton(
  modifier: Modifier = Modifier,
  disabled: Boolean = false,
  upContent: @Composable BoxScope.(text: Color) -> Unit,
  downContent: @Composable BoxScope.(text: Color) -> Unit,
  middleContent: (@Composable BoxScope.(text: Color) -> Unit)? = null,
  upEventHandler: (ControlButtonScope.() -> ControlButtonScope.OnEventHandler)? = null,
  middleEventHandler: (ControlButtonScope.() -> ControlButtonScope.OnEventHandler)? = null,
  downEventHandler: (ControlButtonScope.() -> ControlButtonScope.OnEventHandler)? = null
) {
  ControlButtonScope {
    val upHandler = upEventHandler?.let { it() }
    val downHandler = downEventHandler?.let { it() }
    val middleHandler = middleEventHandler?.let { it() }

    Column(
      modifier = modifier
    ) {
      UpButton(
        disabled = disabled,
        content = upContent,
        onClick = upHandler?.onClick(),
        onTouchDown = upHandler?.onTouchDown(),
        onTouchUp = upHandler?.onTouchUp()
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

      DownButton(
        disabled = disabled,
        content = downContent,
        onClick = downHandler?.onClick(),
        onTouchDown = downHandler?.onTouchDown(),
        onTouchUp = downHandler?.onTouchUp()
      )
    }
  }
}

@Composable
fun BoxScope.UpControlIcon(textColor: Color) =
  ControlButtonIcon(
    iconRes = R.drawable.ic_arrow_right,
    textColor = textColor,
    rotate = 270f,
    modifier = Modifier.align(Alignment.Center)
  )

@Composable
fun BoxScope.StopControlIcon(textColor: Color) =
  ControlButtonIcon(
    iconRes = R.drawable.ic_stop,
    textColor = textColor,
    modifier = Modifier.align(Alignment.Center)
  )

@Composable
fun BoxScope.DownControlIcon(textColor: Color) =
  ControlButtonIcon(
    iconRes = R.drawable.ic_arrow_right,
    textColor = textColor,
    rotate = 90f,
    modifier = Modifier.align(Alignment.Center)
  )

@Composable
private fun UpButton(
  disabled: Boolean,
  content: @Composable BoxScope.(text: Color) -> Unit,
  onClick: (() -> Unit)?,
  onTouchDown: (() -> Unit)?,
  onTouchUp: (() -> Unit)?
) {
  SuplaButton(
    disabled = disabled,
    onClick = onClick ?: {},
    onTouchDown = onTouchDown ?: {},
    onTouchUp = onTouchUp ?: {},
    shape = SuplaButtonDefaults.topRoundedShape(radius = CORNER_RADIUS, minHeight = BUTTON_HEIGHT),
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
  onTouchUp: (() -> Unit)?
) {
  SuplaButton(
    disabled = disabled,
    onClick = onClick ?: {},
    onTouchDown = onTouchDown ?: {},
    onTouchUp = onTouchUp ?: {},
    shape = SuplaButtonDefaults.notRoundedShape(minHeight = MIDDLE_BUTTON_HEIGHT, minWidth = BUTTON_WIDTH),
  ) {
    content(it)
  }
}

@Composable
private fun DownButton(
  disabled: Boolean,
  content: @Composable BoxScope.(text: Color) -> Unit,
  onClick: (() -> Unit)?,
  onTouchDown: (() -> Unit)?,
  onTouchUp: (() -> Unit)?
) {
  SuplaButton(
    disabled = disabled,
    onClick = onClick ?: {},
    onTouchDown = onTouchDown ?: {},
    onTouchUp = onTouchUp ?: {},
    shape = SuplaButtonDefaults.bottomRoundedShape(radius = CORNER_RADIUS, minHeight = BUTTON_HEIGHT),
  ) {
    content(it)
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .padding(24.dp)
    ) {
      UpDownControlButton(
        upContent = { UpControlIcon(textColor = it) },
        downContent = { DownControlIcon(textColor = it) }
      )
      UpDownControlButton(
        disabled = true,
        upContent = { UpControlIcon(textColor = it) },
        downContent = { DownControlIcon(textColor = it) }
      )
      UpDownControlButton(
        disabled = true,
        upContent = { UpControlIcon(textColor = it) },
        downContent = { DownControlIcon(textColor = it) },
        middleContent = { StopControlIcon(textColor = it) }
      )
    }
  }
}
