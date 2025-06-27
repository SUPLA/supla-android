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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Checkbox(
  checked: Boolean,
  label: String,
  enabled: Boolean = true,
  checkedColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
  uncheckedColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
  checkmarkColor: Color = MaterialTheme.colorScheme.onBackground,
  labelColor: Color = MaterialTheme.colorScheme.onBackground,
  onCheckedChange: (Boolean) -> Unit = { }
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Checkbox(
      checked = checked,
      enabled = enabled,
      onCheckedChange = onCheckedChange,
      colors = CheckboxDefaults.colors(
        checkedColor = checkedColor,
        uncheckedColor = uncheckedColor,
        checkmarkColor = checkmarkColor
      )
    )
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = if (enabled) labelColor else MaterialTheme.colorScheme.outline,
      modifier = Modifier
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          enabled = enabled,
          indication = ripple(),
          onClick = { onCheckedChange(!checked) }
        )
    )
  }
}
