package org.supla.android.ui.views
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.ui.views.buttons.BlueTextButton
import org.supla.android.ui.views.buttons.OutlinedButton

@Composable
fun ErrorContent(
  @StringRes message: Int,
  onTryAgainClick: () -> Unit,
  @StringRes secondaryButtonTitle: Int? = null,
  onSecondaryClick: (() -> Unit)? = null
) {
  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      verticalArrangement = Arrangement.spacedBy(Distance.default),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.align(Alignment.Center)
    ) {
      Image(
        painter = painterResource(id = R.drawable.ic_status_error),
        contentDescription = null
      )
      Text(stringResource(message), style = MaterialTheme.typography.bodyLarge)
      BlueTextButton(
        text = stringResource(id = R.string.status_try_again),
        onClick = onTryAgainClick
      )
      if (secondaryButtonTitle != null && onSecondaryClick != null) {
        OutlinedButton(
          text = stringResource(secondaryButtonTitle),
          onClick = onSecondaryClick
        )
      }
    }
  }
}
