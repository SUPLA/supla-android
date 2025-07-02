@file:OptIn(ExperimentalMaterial3Api::class)

package org.supla.android.ui.views.spinner
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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.supla.android.ui.views.TextField

@Composable
fun ExposedDropdownMenuBoxScope.SpinnerTextField(text: String, expanded: Boolean, enabled: Boolean = true, fillMaxWidth: Boolean = false) {
  var textFieldModifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
  if (fillMaxWidth) {
    textFieldModifier = textFieldModifier.fillMaxWidth()
  }
  TextField(
    value = text,
    readOnly = true,
    trailingIcon = {
      SpinnerTrailingIcon(
        expanded = expanded,
        enabled = enabled,
        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
      )
    },
    modifier = textFieldModifier,
    enabled = enabled
  )
}
