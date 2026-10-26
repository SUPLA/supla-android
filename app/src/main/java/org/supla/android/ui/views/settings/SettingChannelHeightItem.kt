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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.texts.BodySmall

data class ChannelHeightOption(
  val label: String,
  val icon: Int
)

@Composable
fun SettingsColumnScope.SettingChannelHeightItem(
  label: String,
  options: List<ChannelHeightOption>,
  selectedIndex: Int,
  modifier: Modifier = Modifier,
  onOptionSelected: (Int) -> Unit
) {
  SettingRow(modifier = modifier) {
    Column(
      verticalArrangement = Arrangement.spacedBy(Distance.tiny),
      modifier = Modifier.fillMaxWidth()
    ) {
      SettingLabel(label)
      Row(horizontalArrangement = Arrangement.spacedBy(Distance.small), modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
          val selected = index == selectedIndex
          val shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 52.dp)
              .clip(shape)
              .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .05f) else MaterialTheme.colorScheme.background)
              .border(width = 1.dp, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, shape)
              .clickable { onOptionSelected(index) }
              .padding(horizontal = Distance.small)
              .padding(top = 2.dp, bottom = Distance.tiny)
          ) {
            Icon(
              painter = painterResource(option.icon),
              contentDescription = null,
              tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            BodySmall(
              text = option.label,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }
  }
}

@SuplaPreview
@Composable
private fun SettingChannelHeightItemPreview() {
  SuplaTheme {
    SettingBlock {
      SettingChannelHeightItem(
        label = stringResource(R.string.cfg_channel_height),
        options = listOf(
          ChannelHeightOption(stringResource(R.string.settings_channel_height_compact), R.drawable.channel_height_small),
          ChannelHeightOption(stringResource(R.string.settings_channel_height_standard), R.drawable.channel_height_normal),
          ChannelHeightOption(stringResource(R.string.settings_channel_height_large), R.drawable.channel_height_big)
        ),
        selectedIndex = 0,
        onOptionSelected = {}
      )
    }
  }
}
