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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.supla.android.R
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton

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
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
fun DialogHeader(title: String) =
  Text(
    text = title,
    style = MaterialTheme.typography.headlineSmall,
    textAlign = TextAlign.Center,
    modifier = Modifier
      .padding(all = dimensionResource(id = R.dimen.distance_default))
      .fillMaxWidth()
  )

@Composable
fun DialogButtonsRow(content: @Composable RowScope.() -> Unit) =
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(all = dimensionResource(id = R.dimen.distance_default)),
    content = content,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.CenterVertically
  )

@Composable
fun DialogDoubleButtons(
  onNegativeClick: () -> Unit,
  onPositiveClick: () -> Unit,
  processing: Boolean = false,
  positiveEnabled: Boolean = true,
  @StringRes negativeTextRes: Int = R.string.cancel,
  @StringRes positiveTextRes: Int = R.string.ok
) =
  DialogButtonsRow {
    OutlinedButton(
      onClick = onNegativeClick,
      text = stringResource(id = negativeTextRes),
      modifier = Modifier.weight(1f),
      enabled = processing.not()
    )

    if (processing) {
      Box(modifier = Modifier.weight(1f)) {
        CircularProgressIndicator(
          modifier = Modifier
            .align(Alignment.Center)
            .size(32.dp)
        )
      }
    } else {
      Button(
        onClick = onPositiveClick,
        text = stringResource(id = positiveTextRes),
        enabled = positiveEnabled,
        modifier = Modifier.weight(1f)
      )
    }
  }
