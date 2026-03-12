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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.TextButton
import org.supla.android.ui.views.icons.Error
import org.supla.android.ui.views.icons.IconOnCircle
import org.supla.android.ui.views.icons.IconType
import org.supla.android.ui.views.texts.TitleLarge

@Composable
fun DialogWithIcon(
  title: String,
  message: String,
  iconType: IconType,
  primaryButtonTitle: String? = stringResource(id = R.string.toolbar_delete),
  secondaryButtonTitle: String? = stringResource(id = R.string.cancel),
  onDismiss: () -> Unit = {},
  onPrimaryClick: () -> Unit = {},
  onSecondaryClick: () -> Unit = {}
) {
  Dialog(onDismiss = onDismiss) {
    IconOnCircle(
      type = iconType,
      modifier = Modifier
        .padding(Distance.default)
        .align(Alignment.CenterHorizontally)
    )

    TitleLarge(
      text = title,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = Distance.default),
      textAlign = TextAlign.Center
    )
    DialogMessage(message = message)
    DialogButtonsColumn(spacing = 4.dp) {
      primaryButtonTitle?.let {
        Button(
          onClick = onPrimaryClick,
          text = it,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      secondaryButtonTitle?.let {
        TextButton(
          onClick = onSecondaryClick,
          text = it,
          modifier = Modifier.fillMaxWidth(),
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  }
}

@Composable
@Preview
private fun Preview() {
  SuplaTheme {
    DialogWithIcon(
      title = stringResource(R.string.notification_delete_all_title),
      message = stringResource(R.string.notification_delete_all_message),
      iconType = Error,
      primaryButtonTitle = stringResource(R.string.notification_delete_all_proceed),
      secondaryButtonTitle = stringResource(R.string.cancel)
    )
  }
}
