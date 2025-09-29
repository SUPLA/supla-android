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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.Switch

@Composable
fun SettingsListItem(
  label: String,
  checked: Boolean,
  modifier: Modifier = Modifier,
  description: String? = null,
  onCheckedChanged: (Boolean) -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .defaultMinSize(minHeight = 64.dp)
      .background(MaterialTheme.colorScheme.background)
      .padding(bottom = 1.dp)
      .background(MaterialTheme.colorScheme.surface)
      .fillMaxWidth()
      .padding(horizontal = Distance.default)
  ) {
    if (description != null) {
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.bodyMedium,
        )
        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.weight(1f)
      )
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChanged
    )
  }
}

@Composable
fun SettingsList(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit
) =
  Column(
    modifier = modifier,
    content = content
  )

@Preview
@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun Preview() {
  SuplaTheme {
    SettingsList {
      SettingsListItem(
        label = "Screen rotation enabled",
        checked = false,
        onCheckedChanged = {}
      )
      SettingsListItem(
        label = "Screen rotation enabled",
        checked = false,
        description = "App restart is needed",
        onCheckedChanged = {}
      )
    }
  }
}
