package org.supla.android.ui.dialogs.state
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogButtonsRow
import org.supla.android.ui.dialogs.DialogHeader
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle
import org.supla.android.ui.views.buttons.OutlinedButton

data class StateDialogViewState(
  val remoteId: Int,
  val title: StringProvider,
  val loading: Boolean = true,
  val values: Map<StateDialogItem, StringProvider> = emptyMap()
)

@Composable
fun StateDialog(
  state: StateDialogViewState,
  onDismiss: () -> Unit
) {
  Dialog(onDismiss = onDismiss) {
    DialogHeader(title = state.title(LocalContext.current))
    Separator(style = SeparatorStyle.LIGHT)

    if (state.loading) {
      CircularProgressIndicator(
        modifier = Modifier
          .align(Alignment.CenterHorizontally)
          .padding(vertical = Distance.default)
          .size(64.dp)
      )
    } else {
      Spacer(modifier = Modifier.height(Distance.small))
      state.values.forEach {
        Row(
          horizontalArrangement = Arrangement.spacedBy(1.dp),
          modifier = Modifier
            .height(IntrinsicSize.Max)
            .padding(horizontal = Distance.tiny)
            .background(MaterialTheme.colorScheme.outline)
        ) {
          Text(
            text = stringResource(id = it.key.captionResource),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
              .background(MaterialTheme.colorScheme.surface)
              .weight(0.5f)
              .padding(horizontal = Distance.tiny)
              .fillMaxHeight()
          )
          Text(
            text = it.value(LocalContext.current),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
              .background(MaterialTheme.colorScheme.surface)
              .weight(0.5f)
              .padding(horizontal = Distance.tiny)
              .fillMaxHeight()
          )
        }
      }
      Spacer(modifier = Modifier.height(Distance.small))
    }

    Separator(style = SeparatorStyle.LIGHT)
    DialogButtonsRow {
      OutlinedButton(
        onClick = onDismiss,
        text = stringResource(id = R.string.channel_btn_close),
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
@Preview
private fun Preview() {
  SuplaTheme {
    StateDialog(
      state = StateDialogViewState(
        remoteId = 1,
        title = { "Dimmer" },
        loading = false,
        values = mapOf(
          StateDialogItem.CHANNEL_ID to { "123456" },
          StateDialogItem.BRIDGE_SIGNAL to { "100%" }
        )
      ),
      onDismiss = {}
    )
  }
}
