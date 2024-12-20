package org.supla.android.features.details.detailbase.impulsecounter
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.ChannelOfflineView
import org.supla.android.ui.views.card.SingleSummaryCard
import org.supla.android.ui.views.card.SummaryCardData

@Composable
fun ImpulseCounterMetricsView(
  state: ImpulseCounterState,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
  ) {
    state.totalData?.let {
      SingleSummaryCard(
        label = stringResource(R.string.details_ic_meter_value),
        data = it,
        modifier = Modifier.padding(start = Distance.default, top = Distance.default, end = Distance.default)
      )
    }
    state.currentMonthData?.let {
      SingleSummaryCard(
        label = stringResource(R.string.details_ic_current_consumption),
        data = it,
        modifier = Modifier.padding(start = Distance.default, top = Distance.default, end = Distance.default),
        loading = state.currentMonthDownloading
      )
    }
    if (state.online == false) {
      ChannelOfflineView()
    }
  }
}

@Preview(showBackground = true)
@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun Preview() {
  SuplaTheme {
    ImpulseCounterMetricsView(
      ImpulseCounterState(
        online = true,
        totalData = SummaryCardData("1234 kWh", "2345 PLN"),
      )
    )
  }
}
