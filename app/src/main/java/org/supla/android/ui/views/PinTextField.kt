package org.supla.android.ui.views
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
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme

const val PIN_LENGTH = 4

@Composable
fun PinTextField(
  pin: String,
  modifier: Modifier = Modifier,
  isError: Boolean = false,
  pinLength: Int = PIN_LENGTH,
  onPinChange: (String, Boolean) -> Unit = { _, _ -> }
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()

  BasicTextField(
    modifier = modifier,
    value = TextFieldValue(pin, selection = TextRange(pinLength)),
    onValueChange = {
      if (it.text.length <= pinLength && it.text != pin) {
        onPinChange.invoke(it.text, it.text.length == pinLength)
      }
    },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
    decorationBox = {
      Row(horizontalArrangement = Arrangement.Center) {
        repeat(pinLength) { index ->
          PinItemView(
            index = index,
            text = pin,
            isError = isError,
            isFieldFocused = isFocused
          )
          Spacer(modifier = Modifier.width(8.dp))
        }
      }
    },
    interactionSource = interactionSource,
    singleLine = true
  )
}

@Composable
private fun PinItemView(
  index: Int,
  text: String,
  isError: Boolean,
  isFieldFocused: Boolean
) {
  val isItemFocused = text.length == index || (text.length == PIN_LENGTH && index == 3)
  val char = when {
    index >= text.length -> ""
    else -> "•"
  }
  val color = when {
    isFieldFocused && isItemFocused -> MaterialTheme.colorScheme.primary
    isError -> MaterialTheme.colorScheme.error
    else -> colorResource(id = R.color.disabled)
  }

  val shape = RoundedCornerShape(4.dp)
  Text(
    modifier = Modifier
      .width(48.dp)
      .height(56.dp)
      .border(1.dp, color, shape)
      .background(MaterialTheme.colorScheme.surface, shape)
      .padding(2.dp)
      .wrapContentHeight(align = Alignment.CenterVertically),
    text = char,
    style = MaterialTheme.typography.headlineLarge,
    color = color,
    textAlign = TextAlign.Center,
  )
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp)
    ) {
      PinTextField(pin = "")
      PinTextField(pin = "1")
      PinTextField(pin = "12")
      PinTextField(pin = "124", isError = true)
      PinTextField(pin = "1245")
    }
  }
}
