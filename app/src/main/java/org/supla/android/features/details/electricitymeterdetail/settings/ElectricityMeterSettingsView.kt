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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.ViewState
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.data.model.settings.ListValueAggregation
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterBalanceType
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterMeasurementType
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.settings.SettingRow
import org.supla.android.ui.views.settings.SettingsHeader
import org.supla.android.ui.views.settings.SettingsList
import org.supla.android.ui.views.spinner.SpinnerItem
import org.supla.android.ui.views.spinner.TextSpinner
import org.supla.core.shared.infrastructure.LocalizedString

data class ElectricityMeterSettingsViewState(
  val channelName: LocalizedString = LocalizedString.Empty,
  val metricOnList: SingleSelectionList<ElectricityMeterMeasurementType>? = null,
  val metricOnListBalancing: SingleSelectionList<ElectricityMeterBalanceType>? = null,
  val metricOnListAggregation: SingleSelectionList<ListValueAggregation>? = null,
  val currentMonthBalancing: SingleSelectionList<ElectricityMeterBalanceType>? = null
) : ViewState()

interface ElectricityMeterSettingsScope {
  fun onMetricOnListChange(type: ElectricityMeterMeasurementType)
  fun onMetricOnListBalancingChanged(type: ElectricityMeterBalanceType)
  fun onMetricOnListAggregationChanged(aggregation: ListValueAggregation)
  fun onCurrentMonthBalancingChanged(type: ElectricityMeterBalanceType)
}

@Composable
fun ElectricityMeterSettingsScope.View(
  state: ElectricityMeterSettingsViewState
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(1.dp),
    modifier = Modifier
      .padding(top = Distance.default, bottom = Distance.default)
      .fillMaxWidth()
  ) {
    SettingsHeader(stringResource(id = R.string.details_settings_title, state.channelName(LocalContext.current)))
    state.currentMonthBalancing?.let { balancingOptions ->
      SettingsList {
        Selector(options = balancingOptions, onOptionSelected = { onCurrentMonthBalancingChanged(it) })
      }
    }

    SettingsHeader(
      stringResource(R.string.details_em_on_list),
      modifier = Modifier.padding(top = Distance.default)
    )
    SettingsList {
      state.metricOnList?.let { options ->
        SettingRow {
          TextSpinner(
            options = options,
            onOptionSelected = { onMetricOnListChange(it) },
            labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        state.metricOnListAggregation?.let { options ->
          SettingRow {
            TextSpinner(
              options = options,
              onOptionSelected = { onMetricOnListAggregationChanged(it) },
              labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        state.metricOnListBalancing?.let { options ->
          SettingRow {
            TextSpinner(
              options = options,
              onOptionSelected = { onMetricOnListBalancingChanged(it) },
              labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

@Composable
private fun <T : SpinnerItem> Selector(options: SingleSelectionList<T>, onOptionSelected: (T) -> Unit) =
  SettingRow {
    TextSpinner(options = options, onOptionSelected = onOptionSelected, labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant)
  }

private val previewScope = object : ElectricityMeterSettingsScope {
  override fun onMetricOnListChange(type: ElectricityMeterMeasurementType) {}
  override fun onMetricOnListBalancingChanged(type: ElectricityMeterBalanceType) {}
  override fun onMetricOnListAggregationChanged(aggregation: ListValueAggregation) {}
  override fun onCurrentMonthBalancingChanged(type: ElectricityMeterBalanceType) {}
}

@SuplaPreview
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      state = ElectricityMeterSettingsViewState(
        channelName = LocalizedString.Constant("Electricity meter"),
        metricOnList = SingleSelectionList(
          selected = ElectricityMeterMeasurementType.FORWARD_ACTIVE_ENERGY,
          items = listOf(
            ElectricityMeterMeasurementType.FORWARD_ACTIVE_ENERGY,
            ElectricityMeterMeasurementType.REVERSE_REACTIVE_ENERGY,
            ElectricityMeterMeasurementType.CURRENT,
            ElectricityMeterMeasurementType.VOLTAGE
          ),
          label = R.string.details_em_settings_list_metric
        ),
        currentMonthBalancing = SingleSelectionList(
          selected = ElectricityMeterBalanceType.VECTOR,
          items = listOf(
            ElectricityMeterBalanceType.VECTOR,
            ElectricityMeterBalanceType.ARITHMETIC,
            ElectricityMeterBalanceType.HOURLY
          ),
          label = R.string.details_em_last_month_balancing
        ),
        metricOnListAggregation = SingleSelectionList(
          selected = ListValueAggregation.NO_AGGREGATION,
          items = ListValueAggregation.entries,
          label = R.string.details_em_on_list_value
        ),
        metricOnListBalancing = SingleSelectionList(
          selected = ElectricityMeterBalanceType.VECTOR,
          items = listOf(
            ElectricityMeterBalanceType.VECTOR,
            ElectricityMeterBalanceType.ARITHMETIC,
            ElectricityMeterBalanceType.HOURLY
          ),
          label = R.string.details_em_on_list_balance
        )
      )
    )
  }
}
