package org.supla.android.features.details.electricitymeterdetail.settings
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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.electricitymeter.ElectricityMeterBalanceType
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.ui.views.spinner.SpinnerItem
import org.supla.android.ui.views.spinner.TextSpinner

data class ElectricityMeterSettingsViewState(
  val channelName: StringProvider = { "" },
  val onListOptions: SingleSelectionList<SuplaElectricityMeasurementType>? = null,
  val balancing: SingleSelectionList<ElectricityMeterBalanceType>? = null
)

@Composable
fun ElectricityMeterSettingsView(
  state: ElectricityMeterSettingsViewState,
  onListValueChanged: (SuplaElectricityMeasurementType) -> Unit = {},
  onBalancingChanged: (ElectricityMeterBalanceType) -> Unit = {}
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(1.dp),
    modifier = Modifier
      .padding(top = Distance.default, bottom = Distance.default)
      .fillMaxWidth()
  ) {
    Text(
      text = stringResource(id = R.string.details_em_settings_title, state.channelName(LocalContext.current)).uppercase(),
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.padding(start = Distance.small, bottom = Distance.tiny, end = Distance.small)
    )
    Column(
      verticalArrangement = Arrangement.spacedBy(1.dp),
      modifier = Modifier
        .background(colorResource(id = R.color.separator))
        .padding(top = 1.dp, bottom = 1.dp)
        .fillMaxWidth()
    ) {
      state.onListOptions?.let { onListOptions ->
        Selector(options = onListOptions, onOptionSelected = onListValueChanged)
      }
      state.balancing?.let { balancingOptions ->
        Selector(options = balancingOptions, onOptionSelected = onBalancingChanged)
      }
    }
  }
}

@Composable
private fun <T : SpinnerItem> Selector(options: SingleSelectionList<T>, onOptionSelected: (T) -> Unit) =
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Distance.small),
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .padding(start = Distance.default, top = Distance.small, end = Distance.default, bottom = Distance.small)
  ) {
    TextSpinner(options = options, onOptionSelected = onOptionSelected, labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant)
  }

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
  SuplaTheme {
    ElectricityMeterSettingsView(
      state = ElectricityMeterSettingsViewState(
        channelName = { "Electricity meter" },
        onListOptions = SingleSelectionList(
          selected = SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY,
          items = listOf(
            SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY,
            SuplaElectricityMeasurementType.REVERSE_REACTIVE_ENERGY,
            SuplaElectricityMeasurementType.CURRENT,
            SuplaElectricityMeasurementType.VOLTAGE
          ),
          label = R.string.details_em_settings_list_item
        ),
        balancing = SingleSelectionList(
          selected = ElectricityMeterBalanceType.VECTOR,
          items = listOf(
            ElectricityMeterBalanceType.VECTOR,
            ElectricityMeterBalanceType.ARITHMETIC,
            ElectricityMeterBalanceType.HOURLY
          ),
          label = R.string.details_em_last_month_balancing
        )
      )
    )
  }
}
