package org.supla.android.features.thermostatdetail.history.ui
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.supla.android.R
import org.supla.android.core.ui.BaseViewProxy
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.thermostatdetail.history.HistoryDetailViewState
import org.supla.android.ui.views.ThermostatChart

interface HistoryDetailProxy : BaseViewProxy<HistoryDetailViewState> {
  fun startDownload() { }
}

@Composable
fun HistoryDetail(viewModel: HistoryDetailProxy) {
  val viewState by viewModel.getViewState().collectAsState()

  Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.distance_default))) {
    Button(onClick = { viewModel.startDownload() }) {
      Text(text = "Start download")
    }
    ThermostatChart(viewState.combinedData(LocalContext.current.resources), modifier = Modifier.weight(1f))
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Row(
        modifier = Modifier.height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_tiny))
      ) {
        Checkbox(
          checked = true,
          onCheckedChange = {},
          colors = CheckboxDefaults.colors(
            checkedColor = colorResource(id = R.color.primary),
            uncheckedColor = colorResource(id = R.color.primary)
          )
        )
        Text(text = "Termometr")
      }
      Row(
        modifier = Modifier.height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_tiny))
      ) {
        Checkbox(
          checked = true,
          onCheckedChange = {},
          colors = CheckboxDefaults.colors(checkedColor = colorResource(id = R.color.red), uncheckedColor = colorResource(id = R.color.red))
        )
        Text(text = "Termometr")
      }
      Row(
        modifier = Modifier.height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_tiny))
      ) {
        Checkbox(
          checked = true,
          onCheckedChange = {},
          colors = CheckboxDefaults.colors(
            checkedColor = colorResource(id = R.color.blue),
            uncheckedColor = colorResource(id = R.color.blue)
          )
        )
        Text(text = "Wilgotność")
      }
    }
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    HistoryDetail(PreviewProxy())
  }
}

private class PreviewProxy : HistoryDetailProxy {
  override fun getViewState(): StateFlow<HistoryDetailViewState> =
    MutableStateFlow(
      value = HistoryDetailViewState()
    )
}
