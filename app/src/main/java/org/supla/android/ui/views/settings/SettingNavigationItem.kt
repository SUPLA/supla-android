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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.Image

@Composable
fun SettingsColumnScope.SettingNavigationItem(
  label: String,
  modifier: Modifier = Modifier,
  value: String? = null,
  valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
  onClick: () -> Unit
) {
  SettingRow(
    modifier = modifier.clickable(onClick = onClick)
  ) {
    value?.let {
      Text(
        text = it,
        color = valueColor,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.widthIn(min = 24.dp).padding(end = Distance.tiny)
      )
    }

    SettingLabel(label, modifier = Modifier.weight(1f))

    Image(
      drawableId = R.drawable.ic_arrow_right,
      modifier = Modifier
        .padding(vertical = Distance.tiny)
        .padding(start = Distance.small)
        .size(dimensionResource(R.dimen.icon_small_size))
    )
  }
}

@SuplaPreview
@Composable
private fun SettingNavigationItemPreview() {
  SuplaTheme {
    SettingsBlock {
      item {
        SettingNavigationItem(
          label = stringResource(R.string.location_ordering),
          onClick = {}
        )
      }
      item {
        SettingNavigationItem(
          label = stringResource(R.string.settings_notifications),
          value = stringResource(R.string.notifications_active),
          valueColor = MaterialTheme.colorScheme.primary,
          onClick = {}
        )
      }
      item {
        SettingNavigationItem(
          label = stringResource(R.string.settings_localization),
          value = stringResource(R.string.notifications_inactive),
          valueColor = MaterialTheme.colorScheme.error,
          onClick = {}
        )
      }
    }
  }
}
