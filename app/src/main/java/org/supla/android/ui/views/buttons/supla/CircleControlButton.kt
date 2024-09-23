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

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.disabledOverlay

@Composable
fun CircleControlButton(
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
