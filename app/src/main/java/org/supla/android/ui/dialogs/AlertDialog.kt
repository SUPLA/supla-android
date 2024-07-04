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
syays GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton

@Composable
fun AlertDialog(
  title: String,
  message: String,
  negativeButtonTitle: String? = stringResource(id = R.string.cancel),
  positiveButtonTitle: String? = stringResource(id = R.string.save),
  onDismiss: () -> Unit = {},
  onNegativeClick: () -> Unit = {},
  onPositiveClick: () -> Unit = {}
) {
  Dialog(onDismiss = onDismiss) {
    DialogHeader(title = title)
    Separator(style = SeparatorStyle.LIGHT)
    DialogMessage(message = message)
    Separator(style = SeparatorStyle.LIGHT)
    DialogButtonsRow {
      negativeButtonTitle?.let {
        OutlinedButton(
          onClick = onNegativeClick,
          text = it,
          modifier = Modifier.weight(1f)
        )
      }
      positiveButtonTitle?.let {
        Button(
          onClick = onPositiveClick,
          text = it,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun DialogMessage(message: String) =
  Text(
    text = message,
    style = MaterialTheme.typography.body2,
    textAlign = TextAlign.Center,
    modifier = Modifier
      .padding(all = Distance.default)
      .fillMaxWidth()
  )
