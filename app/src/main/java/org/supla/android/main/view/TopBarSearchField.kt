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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.main.topbar.LocalTopBarController
import org.supla.android.ui.views.buttons.IconButton
import org.supla.android.ui.views.forms.TextField

@Composable
fun TopBarSearchField(
  searchText: String,
  selectionResetKey: Any?,
  modifier: Modifier = Modifier
) {
  val topBarController = LocalTopBarController.current
  var textFieldValue by remember { mutableStateOf(searchText.textFieldValueAtEnd()) }
  var currentSelectionResetKey by remember { mutableStateOf(selectionResetKey) }

  // Used to position cursor at the end after changing a tab in main list screen
  LaunchedEffect(searchText, selectionResetKey) {
    if (textFieldValue.text != searchText || currentSelectionResetKey != selectionResetKey) {
      textFieldValue = searchText.textFieldValueAtEnd()
    }
    currentSelectionResetKey = selectionResetKey
  }

  TextField(
    value = textFieldValue,
    onValueChange = { value ->
      textFieldValue = value
      if (value.text != searchText) {
        topBarController.updateSearchValue(value.text)
      }
    },
    modifier = modifier.height(40.dp),
    placeholder = {
      Text(
        text = stringResource(R.string.toolbar_search_hint),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    },
    trailingIcon = {
      if (searchText.isNotEmpty()) {
        IconButton(
          icon = R.drawable.ic_close,
          onClick = { topBarController.updateSearchValue("") },
          contentDescription = stringResource(R.string.general_clear_search),
          modifier = Modifier.size(40.dp),
          iconSize = 16.dp,
          tint = MaterialTheme.colorScheme.onBackground
        )
      }
    },
    singleLine = true,
    textStyle = MaterialTheme.typography.bodyLarge,
    contentPadding = PaddingValues(horizontal = Distance.small, vertical = 4.dp)
  )
}

private fun String.textFieldValueAtEnd() =
  TextFieldValue(text = this, selection = TextRange(length))
