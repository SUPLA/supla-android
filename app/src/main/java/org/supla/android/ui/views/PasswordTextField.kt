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

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun PasswordTextField(
  password: String,
  modifier: Modifier = Modifier,
  passwordVisible: Boolean = false,
  isError: Boolean = false,
  enabled: Boolean = true,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  onVisibilityChange: () -> Unit,
  label: @Composable (() -> Unit)? = null,
  onValueChange: (String) -> Unit = { }
) {
  TextField(
    value = password,
    modifier = modifier.semantics { contentType = ContentType.Password },
    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
    label = label,
    keyboardOptions = keyboardOptions.copy(keyboardType = KeyboardType.Password),
    keyboardActions = keyboardActions,
    singleLine = true,
    onValueChange = onValueChange,
    isError = isError,
    enabled = enabled,
    trailingIcon = {
      IconButton(onClick = onVisibilityChange) {
        Icon(
          imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
          contentDescription = null
        )
      }
    }
  )
}
