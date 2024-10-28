package org.supla.android.features.appsettings.view
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.layouts.BaseAbstractComposeView
import org.supla.android.ui.views.TextField

class SettingsItemEditText : BaseAbstractComposeView {

  constructor(context: Context) : super(context, null, 0)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0)

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onValueChanged: (Int) -> Unit = {}

  var label by mutableStateOf("")
  var value by mutableStateOf("")
  var isError by mutableStateOf(false)
  var suffix: String? by mutableStateOf("")
  var prefix: String? by mutableStateOf("")

  @Composable
  override fun Content() {
    SuplaTheme {
      Row(
        horizontalArrangement = Arrangement.spacedBy(Distance.default),
        verticalAlignment = Alignment.CenterVertically
      ) {
        val focusManager = LocalFocusManager.current
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        TextField(
          value = value,
          modifier = Modifier
            .width(100.dp)
            .padding(vertical = Distance.tiny),
          keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
          isError = isError,
          singleLine = true,
          onValueChange = {
            val newValue = it.filter { char -> char.isDigit() }
            if (newValue.length < 3) {
              value = newValue
              it.toIntOrNull()?.let(onValueChanged)
            }
          },
          suffix = { suffix?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) } },
          prefix = { prefix?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) } },
          textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
          contentPadding = PaddingValues(horizontal = Distance.small, vertical = Distance.tiny),
          focusedColor = MaterialTheme.colorScheme.primary,
          keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
          )
        )
      }
    }
  }
}
