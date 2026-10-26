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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.BoxSize
import org.supla.android.ui.views.SegmentedComponent
import org.supla.android.ui.views.SegmentedComponentItem
import org.supla.core.shared.infrastructure.LocalizedString

@Composable
fun SettingsColumnScope.SettingSegmentedItem(
  label: String,
  options: List<String>,
  selectedIndex: Int,
  modifier: Modifier = Modifier,
  onOptionSelected: (Int) -> Unit
) {
  val items = options.mapIndexed { index, option -> SettingSegmentOption(index, LocalizedString.Constant(option)) }

  SettingRow(modifier = modifier) {
    SettingLabel(label, modifier = Modifier.weight(1f))
    SegmentedComponent(
      items = items,
      activeItem = items.getOrNull(selectedIndex),
      boxSize = BoxSize.Custom,
      onClick = { onOptionSelected(it.index) }
    )
  }
}

private data class SettingSegmentOption(
  val index: Int,
  override val label: LocalizedString
) : SegmentedComponentItem

@SuplaPreview
@Composable
private fun SettingSegmentedItemPreview() {
  SuplaTheme {
    SettingBlock {
      SettingSegmentedItem(
        label = stringResource(R.string.cfg_temperature_unit),
        options = listOf("°C", "°F"),
        selectedIndex = 1,
        onOptionSelected = {}
      )
    }
  }
}
