package org.supla.android.main.view
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaComponentPreview
import org.supla.android.ui.views.buttons.DrawerBackButton
import org.supla.android.ui.views.buttons.IconButton

@Composable
fun SearchTopBar(
  searchText: String,
  onBackClick: () -> Unit,
  onTextChange: (String) -> Unit,
  rightIcon: (@Composable () -> Unit)? = null
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(horizontal = Distance.default, vertical = Distance.small)
      .shadow(elevation = 4.dp, shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))),
    shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default)),
    color = MaterialTheme.colorScheme.surface
  ) {
    Row(
      modifier = Modifier,
      verticalAlignment = Alignment.CenterVertically
    ) {
      DrawerBackButton(onClick = onBackClick)

      OutlinedTextField(
        value = searchText,
        onValueChange = { onTextChange(it) },
        modifier = Modifier.weight(1f),
        placeholder = {
          Text(
            text = "${stringResource(R.string.general_search)}...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = Color.Transparent,
          disabledBorderColor = Color.Transparent,
          unfocusedBorderColor = Color.Transparent
        )
      )

      rightIcon?.invoke()
    }
  }
}

@SuplaComponentPreview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(Modifier.background(MaterialTheme.colorScheme.outline)) {
      SearchTopBar(
        searchText = "",
        onBackClick = {},
        onTextChange = {}
      )
      SearchTopBar(
        searchText = "",
        onBackClick = {},
        onTextChange = {},
        rightIcon = {
          IconButton(
            R.drawable.ic_menu_profiles,
            onClick = { },
            contentDescription = stringResource(R.string.profile_plural)
          )
        }
      )
    }
  }
}
