package org.supla.android.ui.views.settings
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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.supla.android.core.ui.theme.Distance
import org.supla.android.ui.views.forms.TextField

@Composable
fun SettingTextFieldItem(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
  description: String? = null,
  unit: String? = null,
  incorrectValue: Boolean = false,
  onValueChanged: (String) -> Unit
) {
  SettingRow(modifier = modifier) {
    if (description != null) {
      Column(
        modifier = Modifier.weight(1f)
      ) {
        SettingLabel(label)
        SettingDescription(description)
      }
    } else {
      SettingLabel(
        label = label,
        modifier = Modifier.weight(1f)
      )
    }

    TextField(
      value = value,
      onValueChange = onValueChanged,
      textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.End),
      suffix = unit?.let { { UnitText(it) } },
      contentPadding = PaddingValues(horizontal = Distance.small, vertical = Distance.tiny),
      modifier = Modifier.width(80.dp),
      isError = incorrectValue,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
  }
}

@Composable
private fun UnitText(text: String) =
  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant
  )
