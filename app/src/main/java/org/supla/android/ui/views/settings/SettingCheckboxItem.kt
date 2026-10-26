package org.supla.android.ui.views.settings
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

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.supla.android.core.ui.theme.Distance
import org.supla.android.ui.views.Switch

@Composable
fun SettingsColumnScope.SettingCheckboxItem(
  label: String,
  checked: Boolean,
  modifier: Modifier = Modifier,
  description: String? = null,
  onLabelLongClick: (() -> Unit)? = null,
  onCheckedChanged: (Boolean) -> Unit
) {
  SettingRow(modifier = modifier) {
    if (description != null) {
      Column(
        modifier = Modifier.weight(1f)
      ) {
        SettingLabel(
          label = label,
          modifier = Modifier
            .combinedClickable(enabled = onLabelLongClick != null, onClick = {}, onLongClick = { onLabelLongClick?.invoke() })
        )
        SettingDescription(description)
      }
    } else {
      SettingLabel(
        label = label,
        modifier = Modifier
          .weight(1f)
          .combinedClickable(enabled = onLabelLongClick != null, onClick = {}, onLongClick = { onLabelLongClick?.invoke() })
          .padding(vertical = Distance.tiny)
      )
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChanged
    )
  }
}
