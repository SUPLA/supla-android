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
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.SuplaTypography
import org.supla.android.ui.views.buttons.IconWrapper

// special colors
private val disabledOverlay = Color(0xDDFFFFFF)

class RoundedControlButtonView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

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
      RoundedControlButton(text = text, type = type, onClick = clickListener, disabled = disabled)
    }
  }
}

@Composable
fun RoundedControlButton(
  modifier: Modifier = Modifier,
  icon: Painter? = null,
  iconColor: Color? = null,
  text: String? = null,
  backgroundColor: Color = MaterialTheme.colors.surface,
  type: AnimatableButtonType = AnimatableButtonType.POSITIVE,
  disabled: Boolean = false,
  animationMode: AnimationMode = AnimationMode.Pressed,
  iconAndTextColorSynced: Boolean = false,
  height: Dp = dimensionResource(id = R.dimen.button_default_size),
  padding: Dp = 0.dp,
  contentPadding: PaddingValues = PaddingValues(all = 0.dp),
  fontSize: TextUnit = TextUnit.Unspecified,
  fontFamily: FontFamily? = null,
  onLongClick: () -> Unit = {},
  onClick: () -> Unit
) {
  SimpleButtonColorAnimatable(
    modifier = modifier,
    type = type,
    height = height,
    padding = padding,
    disabled = disabled,
    backgroundColor = backgroundColor,
    animationMode = animationMode,
    onClick = {
      if (!disabled) {
        onClick()
      }
    },
    onLongClick = {
      if (!disabled) {
        onLongClick()
      }
    }
  ) { textColor ->
    Row(
      modifier = Modifier.align(Alignment.Center).padding(contentPadding),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconWrapper(painter = icon, color = if (iconAndTextColorSynced) textColor else iconColor)
      text?.let {
        Text(text = text, style = SuplaTypography.button, color = textColor, fontSize = fontSize, fontFamily = fontFamily)
      }
    }

    if (disabled) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(color = disabledOverlay)
      )
    }
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(modifier = Modifier.background(color = Color(0xFFF5F6F7))) {
      RoundedControlButton(onClick = {})
      RoundedControlButton(icon = painterResource(id = R.drawable.ic_power_button), text = "Turn on", onClick = {})
      RoundedControlButton(text = "Turn on", onClick = {}, disabled = true)
    }
  }
}
