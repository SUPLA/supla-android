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

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.main.topbar.LocalTopBarController
import org.supla.android.ui.views.buttons.IconButton

@Composable
fun TopBarSearchField(
  searchText: String,
  modifier: Modifier = Modifier
) {
  val topBarController = LocalTopBarController.current

  OutlinedTextField(
    value = searchText,
    onValueChange = topBarController::updateSearchValue,
    modifier = modifier,
    placeholder = {
      Text(
        text = stringResource(R.string.toolbar_search_hint),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    },
    singleLine = true,
    colors = OutlinedTextFieldDefaults.colors(
      focusedBorderColor = Color.Transparent,
      disabledBorderColor = Color.Transparent,
      unfocusedBorderColor = Color.Transparent
    )
  )

  IconButton(
    icon = R.drawable.ic_close,
    onClick = {
      if (searchText.isEmpty()) {
        topBarController.setSearchVisible(false)
      } else {
        topBarController.updateSearchValue("")
      }
    },
    contentDescription = stringResource(R.string.general_clear_search),
    modifier = Modifier.size(40.dp),
    iconSize = 16.dp
  )
}
