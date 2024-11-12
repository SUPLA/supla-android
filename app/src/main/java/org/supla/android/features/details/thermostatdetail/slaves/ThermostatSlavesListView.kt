package org.supla.android.features.details.thermostatdetail.slaves
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.formatting.LocalPercentageFormatter
import org.supla.android.data.source.remote.thermostat.ThermostatIndicatorIcon
import org.supla.android.images.ImageId
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.lists.data.message
import org.supla.android.ui.lists.onlineState
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.list.ListItemDot
import org.supla.android.ui.views.list.components.ListItemIcon
import org.supla.android.ui.views.list.components.ListItemInfoIcon
import org.supla.android.ui.views.list.components.ListItemIssueIcon
import org.supla.android.ui.views.list.components.ListItemTitle
import org.supla.android.ui.views.list.components.ListItemValue
import org.supla.android.ui.views.list.components.SetpointIndicator
import org.supla.android.ui.views.list.components.SetpointText
import org.supla.core.shared.data.model.lists.ChannelIssueItem

data class ThermostatSlavesListViewState(
  val master: ThermostatData? = null,
  val slaves: List<ThermostatData> = emptyList(),
  val scale: Float = 1f
)

data class ThermostatData(
  val channelId: Int,
  val onlineState: ListOnlineState,
  val caption: StringProvider,
  val imageId: ImageId,
  val currentPower: Float?,
  val value: String,
  val indicatorIcon: ThermostatIndicatorIcon,
  val channelIssueItem: List<ChannelIssueItem>,
  val showChannelStateIcon: Boolean,
  val subValue: String? = null,
  val pumpSwitchIcon: ImageId? = null,
  val sourceSwitchIcon: ImageId? = null
)

@Composable
fun ThermostatSlavesListView(
  state: ThermostatSlavesListViewState,
  onShowMessage: (String) -> Unit,
  onShowInfo: (ThermostatData) -> Unit
) {
  Column {
    state.master?.let {
      Text(
        text = stringResource(id = R.string.thermostat_detail_main_thermostat).uppercase(),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(start = Distance.small, bottom = Distance.tiny, end = Distance.small, top = Distance.default)
      )
      SlaveRow(slave = it, scale = state.scale, onShowMessage = onShowMessage, onShowInfo = onShowInfo)
    }
    Text(
      text = stringResource(id = R.string.thermostat_detail_other_thermostats).uppercase(),
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.padding(start = Distance.small, bottom = Distance.tiny, end = Distance.small, top = Distance.default)
    )
    LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
      items(
        items = state.slaves,
        key = { it.channelId }
      ) { SlaveRow(slave = it, scale = state.scale, onShowMessage = onShowMessage, onShowInfo = onShowInfo) }
    }
  }
}

@Composable
private fun SlaveRow(slave: ThermostatData, scale: Float, onShowMessage: (String) -> Unit, onShowInfo: (ThermostatData) -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .padding(vertical = Distance.tiny)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(Distance.small),
    ) {
      Column(
        modifier = Modifier.padding(start = Distance.small),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        ListItemIcon(imageId = slave.imageId, scale = scale)
        Text(
          text = slave.currentPower?.let { LocalPercentageFormatter.current.format(it) } ?: "",
          style = MaterialTheme.typography.bodySmall
        )
      }
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
        ) {
          ListItemValue(value = slave.value, scale = scale)
          if (slave.indicatorIcon == ThermostatIndicatorIcon.OFF) {
            SetpointText(subValue = "Off", scale = scale)
          } else {
            SetpointIndicator(indicatorIcon = slave.indicatorIcon.resource, scale = scale)
            slave.subValue?.let { SetpointText(subValue = it, scale = scale) }
          }
          slave.pumpSwitchIcon?.let { Image(imageId = it, modifier = Modifier.size(dimensionResource(id = R.dimen.icon_default_size))) }
          slave.sourceSwitchIcon?.let { Image(imageId = it, modifier = Modifier.size(dimensionResource(id = R.dimen.icon_default_size))) }
        }
        ListItemTitle(
          text = slave.caption(LocalContext.current),
          onLongClick = { },
          onItemClick = { },
          modifier = Modifier.padding(top = Distance.tiny, end = Distance.default)
        )
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(Distance.small),
      modifier = Modifier.align(Alignment.CenterEnd)
    ) {
      slave.channelIssueItem.firstOrNull()?.let {
        val message = slave.channelIssueItem.message(LocalContext.current)
        ListItemIssueIcon(
          icon = it.icon,
          modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(),
            onClick = { onShowMessage(message) }
          )
        )
      }
      if (slave.showChannelStateIcon && slave.onlineState.online) {
        ListItemInfoIcon(onClick = { onShowInfo(slave) })
      }
      ListItemDot(onlineState = slave.onlineState, withButton = false, paddingValues = PaddingValues(end = Distance.small))
    }
  }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xF5F6F7)
@PreviewScreenSizes
@PreviewFontScale
private fun Preview() {
  SuplaTheme {
    ThermostatSlavesListView(
      state = ThermostatSlavesListViewState(
        master = sampleSlave(0),
        slaves = listOf(sampleSlave(1), sampleSlave(2))
      ),
      onShowMessage = {},
      onShowInfo = {}
    )
  }
}

private fun sampleSlave(channelId: Int) = ThermostatData(
  channelId,
  ((channelId % 2) == 0).onlineState,
  { "FHC #$channelId" },
  ImageId(R.drawable.fnc_thermostat_heat),
  25f,
  "22,7°C",
  ThermostatIndicatorIcon.HEATING,
  listOf(ChannelIssueItem.Warning()),
  true,
  pumpSwitchIcon = ImageId(R.drawable.fnc_pump_switch_on),
  sourceSwitchIcon = ImageId(R.drawable.fnc_heat_or_cold_source_switch_off)
)
