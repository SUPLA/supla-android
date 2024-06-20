package org.supla.android.ui.views.buttons
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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OutlinedButton(
  text: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  onClick: () -> Unit
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.button,
      color = MaterialTheme.colors.primary
    )
  }
}

@Composable
fun OutlinedButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  content: @Composable RowScope.() -> Unit
) {
  androidx.compose.material.OutlinedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    border = BorderStroke(1.dp, MaterialTheme.colors.primary),
    content = content
  )
}
