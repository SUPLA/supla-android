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

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.disabledOverlay

class CircleControlButtonView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

  var icon: Bitmap? by mutableStateOf(null)
  var text: String? by mutableStateOf(null)
  var type: Type by mutableStateOf(Type.POSITIVE)
  var disabled: Boolean by mutableStateOf(false)
  var clickListener: () -> Unit = { }

  init {
    context.theme.obtainStyledAttributes(attrs, R.styleable.PowerButtonView, 0, 0).apply {
      try {
        text = getString(R.styleable.PowerButtonView_text)
        type = Type.from(getInteger(R.styleable.PowerButtonView_type, 0))
      } finally {
        recycle()
      }
    }
  }

  @Composable
  override fun Content() {
    SuplaTheme {
      CircleControlButton(
        modifier = Modifier.padding(Distance.tiny),
        icon = icon,
        text = text,
        colors = type.colors(),
        onClick = clickListener,
        disabled = disabled
      )
    }
  }

  enum class Type(val value: Int) {
    POSITIVE(0), NEGATIVE(1);

    @Composable
    fun colors(): SuplaButtonColors = when (this) {
      POSITIVE -> SuplaButtonDefaults.primaryColors(contentDisabled = MaterialTheme.colorScheme.onBackground)
      NEGATIVE -> SuplaButtonDefaults.errorColors(contentDisabled = MaterialTheme.colorScheme.onBackground)
    }

    companion object {
      fun from(value: Int): Type {
        return if (value == NEGATIVE.value) {
          NEGATIVE
        } else {
          POSITIVE
        }
      }
    }
  }
}

@Composable
private fun CircleControlButton(
  modifier: Modifier = Modifier,
  icon: Bitmap? = null,
  text: String? = null,
  disabled: Boolean = false,
  colors: SuplaButtonColors = SuplaButtonDefaults.buttonColors(),
  onClick: () -> Unit,
) {
  SuplaButton(
    onClick = onClick,
    modifier = modifier,
    disabled = disabled,
    colors = colors,
    shape = SuplaButtonDefaults.allRoundedShape(radius = 60.dp)
  ) { contentColor ->
    Column(
      modifier = Modifier
        .align(Alignment.Center)
        .disabledOverlay(disabled),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      icon?.let {
        Image(
          bitmap = it.asImageBitmap(),
          contentDescription = null,
          alignment = Alignment.Center,
          modifier = Modifier.size(dimensionResource(id = R.dimen.icon_default_size))
        )
      }
      text?.let {
        Text(text = it, style = MaterialTheme.typography.labelLarge, color = contentColor)
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  SuplaTheme {
    Column {
      CircleControlButton(onClick = {})
      CircleControlButton(text = "Turn on", onClick = {})
      CircleControlButton(text = "Turn on", onClick = {}, disabled = true)
    }
  }
}
