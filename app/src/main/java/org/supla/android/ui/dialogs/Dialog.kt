package org.supla.android.ui.dialogs
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.supla.android.R

@Composable
fun Dialog(
  modifier: Modifier = Modifier,
  usePlatformDefaultWidth: Boolean = true,
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
  onDismiss: () -> Unit,
  content: @Composable ColumnScope.() -> Unit
) {
  Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = usePlatformDefaultWidth)) {
    Card(
      modifier = modifier,
      elevation = dimensionResource(id = R.dimen.segmented_button_elevation),
      shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_default))
    ) {
      Column(
        horizontalAlignment = horizontalAlignment,
        content = content
      )
    }
  }
}

@Composable
fun DialogButtonsRow(content: @Composable RowScope.() -> Unit) =
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(all = dimensionResource(id = R.dimen.distance_default)),
    content = content,
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  )
