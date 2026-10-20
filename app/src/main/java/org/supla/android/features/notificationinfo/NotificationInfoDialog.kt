package org.supla.android.features.notificationinfo
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.texts.BodyMedium
import org.supla.android.ui.views.texts.HeadlineMedium

@Composable
fun NotificationInfoDialog(
  onTurnOn: () -> Unit = {},
  onSkip: () -> Unit = {}
) {
  Dialog(
    onDismiss = {}
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(Distance.small),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(Distance.default)
    ) {
      Image(
        drawableId = R.drawable.notification_info,
        modifier = Modifier.size(80.dp)
      )
      HeadlineMedium(stringRes = R.string.notification_info_title)
      BodyMedium(stringRes = R.string.notification_info_message)

      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedButton(
          text = stringResource(R.string.skip),
          onClick = onSkip
        )

        Button(
          text = stringResource(R.string.turn_on),
          onClick = onTurnOn
        )
      }
    }
  }
}

@Composable
@SuplaPreview
private fun Preview() {
  SuplaTheme {
    NotificationInfoDialog()
  }
}
