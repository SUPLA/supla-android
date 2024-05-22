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

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.buttons.IconWrapper

class CircleControlButtonView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

  var icon: Bitmap? by mutableStateOf(null)
  var text: String? by mutableStateOf(null)
  var type: AnimatableButtonType by mutableStateOf(AnimatableButtonType.POSITIVE)
  var disabled: Boolean by mutableStateOf(false)
  var clickListener: () -> Unit = { }

  init {
    context.theme.obtainStyledAttributes(attrs, R.styleable.PowerButtonView, 0, 0).apply {
      try {
        text = getString(R.styleable.PowerButtonView_text)
        type = getInteger(R.styleable.PowerButtonView_type, 0).toAnimatableButtonType()
      } finally {
        recycle()
      }
    }
  }

  @Composable
  override fun Content() {
    SuplaTheme {
      CircleControlButton(icon = icon, text = text, type = type, onClick = clickListener, disabled = disabled)
    }
  }
}

@Composable
fun CircleControlButton(
  modifier: Modifier = Modifier,
  icon: Bitmap? = null,
  text: String? = null,
  iconPainter: Painter? = null,
  type: AnimatableButtonType = AnimatableButtonType.POSITIVE,
  onClick: () -> Unit,
  disabled: Boolean = false,
  animationMode: AnimationMode = AnimationMode.Pressed,
  width: Dp = 140.dp,
  height: Dp = 140.dp,
  padding: Dp = 10.dp,
  iconColor: Color? = null
) {
  CircleControlButton(
    modifier = modifier,
    type = type,
    onClick = onClick,
    disabled = disabled,
    animationMode = animationMode,
    width = width,
    height = height,
    padding = padding,
  ) { textColor ->
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      IconWrapper(bitmap = icon, painter = iconPainter, color = iconColor)
      text?.let {
        Text(text = it, style = MaterialTheme.typography.button, color = textColor)
      }
    }
  }
}

@Composable
fun CircleControlButton(
  modifier: Modifier = Modifier,
  type: AnimatableButtonType = AnimatableButtonType.POSITIVE,
  onClick: () -> Unit,
  disabled: Boolean = false,
  animationMode: AnimationMode = AnimationMode.Pressed,
  width: Dp = 140.dp,
  height: Dp = 140.dp,
  padding: Dp = 10.dp,
  content: @Composable BoxScope.(textColor: Color) -> Unit
) {
  SimpleButtonColorAnimatable(
    modifier = modifier.width(width),
    type = type,
    height = height,
    padding = padding,
    disabled = disabled,
    animationMode = animationMode,
    onClick = {
      if (!disabled) {
        onClick()
      }
    }
  ) { textColor ->
    content(textColor)
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(modifier = Modifier.background(color = MaterialTheme.colors.background)) {
      CircleControlButton(onClick = {})
      CircleControlButton(text = "Turn on", onClick = {})
      CircleControlButton(text = "Turn on", onClick = {}, disabled = true)
    }
  }
}
