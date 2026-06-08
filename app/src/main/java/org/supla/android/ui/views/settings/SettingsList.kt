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
import androidx.compose.foundation.layout.RowScope
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

@Composable
fun SettingsList(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit
) =
  Column(
    modifier = modifier,
    content = content
  )

@Composable
fun SettingLabel(
  label: String,
  modifier: Modifier = Modifier
) =
  Text(
    text = label,
    style = MaterialTheme.typography.bodyMedium,
    modifier = modifier
  )

@Composable
fun SettingDescription(
  description: String
) =
  Text(
    text = description,
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
  )

@Composable
fun SettingRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) =
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .defaultMinSize(minHeight = 64.dp)
      .background(MaterialTheme.colorScheme.background)
      .padding(bottom = 1.dp)
      .background(MaterialTheme.colorScheme.surface)
      .fillMaxWidth()
      .padding(horizontal = Distance.default),
    content = content
  )

@Composable
fun SettingsHeader(
  text: String,
  modifier: Modifier = Modifier
) =
  Text(
    text = text.uppercase(),
    style = MaterialTheme.typography.bodyMedium,
    modifier = modifier.padding(start = Distance.small, bottom = Distance.tiny, end = Distance.small)
  )

@Preview
@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun Preview() {
  SuplaTheme {
    SettingsList {
      SettingCheckboxItem(
        label = "Screen rotation enabled",
        checked = false,
        onCheckedChanged = {}
      )
      SettingCheckboxItem(
        label = "Screen rotation enabled",
        checked = false,
        description = "App restart is needed",
        onCheckedChanged = {}
      )
      SettingTextFieldItem(
        label = "Reset hour",
        value = "1",
        onValueChanged = {},
        unit = ":00",
      )
    }
  }
}
