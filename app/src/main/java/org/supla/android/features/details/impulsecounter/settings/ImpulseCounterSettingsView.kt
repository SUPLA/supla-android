package org.supla.android.features.details.impulsecounter.settings
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.data.model.settings.ListValueAggregation
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.settings.SettingRow
import org.supla.android.ui.views.settings.SettingsHeader
import org.supla.android.ui.views.settings.SettingsList
import org.supla.android.ui.views.spinner.TextSpinner
import org.supla.core.shared.infrastructure.LocalizedString

interface ImpulseCounterSettingsViewScope {
  fun onListValueChanged(type: ListValueAggregation)
}

@Composable
fun ImpulseCounterSettingsViewScope.View(
  state: ImpulseCounterSettingsViewState
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(1.dp),
    modifier = Modifier
      .padding(top = Distance.default, bottom = Distance.default)
      .fillMaxWidth()
  ) {
    SettingsHeader(state.title())
    SettingsList {
      SettingRow {
        TextSpinner(options = state.listValueAggregationOptions, onOptionSelected = { onListValueChanged(it) })
      }
    }
  }
}

private val previewScope = object : ImpulseCounterSettingsViewScope {
  override fun onListValueChanged(type: ListValueAggregation) {}
}

@SuplaPreview
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      state = ImpulseCounterSettingsViewState(
        title = LocalizedString.Constant("Settings - Impulse counter"),
        listValueAggregationOptions = SingleSelectionList(
          selected = ListValueAggregation.CURRENT_MONTH,
          items = ListValueAggregation.entries,
          label = R.string.details_ic_settings_list_item
        )
      )
    )
  }
}
