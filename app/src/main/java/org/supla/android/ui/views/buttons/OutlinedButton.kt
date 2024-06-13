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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import org.supla.android.R

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
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.primary
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
  androidx.compose.material3.OutlinedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_default)),
    content = content,
    colors = ButtonDefaults.outlinedButtonColors().copy(containerColor = Color.Transparent)
  )
}
