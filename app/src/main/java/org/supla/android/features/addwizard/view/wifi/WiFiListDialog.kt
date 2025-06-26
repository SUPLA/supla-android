package org.supla.android.features.addwizard.view.wifi
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.isNotNull
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogButtonsRow
import org.supla.android.ui.dialogs.DialogHeader
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.buttons.TextButton

data class WiFiListDialogState(
  val selected: String? = null,
  val items: List<String>? = null,
  val warning: Boolean = false,
  val skipAndConnect: Boolean = false
)

interface WiFiListDialogScope {
  fun onWiFiListDismiss()
  fun onWiFiListCancel()
  fun onWiFiListSelect()
  fun onSsidSelected(ssid: String)
  fun onForceNextClick()
}

@Composable
fun WiFiListDialogScope.WiFiListDialog(
  state: WiFiListDialogState
) {
  Dialog(onDismiss = { onWiFiListDismiss() }, horizontalAlignment = Alignment.CenterHorizontally) {
    DialogHeader(stringResource(R.string.add_wizard_network_scan))

    if (state.warning) {
      Warning()
    }

    state.items?.let { ssids ->
      if (ssids.isEmpty()) {
        Text(
          text = stringResource(R.string.add_wizard_network_scan_empty),
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(horizontal = Distance.default),
          textAlign = TextAlign.Center
        )
      } else {
        LazyColumn(
          modifier = Modifier.padding(horizontal = Distance.default)
        ) {
          itemsIndexed(ssids) { index, item ->
            SsidItem(item, item == state.selected)
            if (index < ssids.size - 1) {
              Spacer(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(1.dp)
                  .background(color = MaterialTheme.colorScheme.outline)
              )
            }
          }
        }
      }
    } ?: ScanningIndicator()

    if (state.skipAndConnect) {
      Text(
        text = stringResource(R.string.add_wizard_network_manual_connect),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = Distance.default).padding(top = Distance.small),
        textAlign = TextAlign.Center
      )
      TextButton(onClick = { onForceNextClick() }, text = stringResource(R.string.next))
    }

    Buttons(state)
  }
}

@Composable
private fun Warning() =
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
    modifier = Modifier
      .padding(horizontal = Distance.default)
      .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default)))
      .border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.error,
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
      )
      .padding(horizontal = Distance.tiny, vertical = Distance.small)
  ) {
    Image(
      drawableId = R.drawable.channel_warning_level1,
      modifier = Modifier.size(dimensionResource(R.dimen.icon_default_size))
    )
    Text(
      text = stringResource(R.string.add_wizard_network_scan_limit_warning),
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.weight(1f)
    )
  }

@Composable
private fun ScanningIndicator() =
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Distance.small)
  ) {
    CircularProgressIndicator()
    Text(
      text = stringResource(R.string.add_wizard_network_scan_in_progress),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }

@Composable
private fun WiFiListDialogScope.SsidItem(item: String, selected: Boolean) =
  Row(
    modifier = Modifier
      .padding(vertical = 8.dp)
      .clickable { onSsidSelected(item) }
  ) {
    Text(
      text = item,
      style = MaterialTheme.typography.bodyMedium,
      color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.weight(1f)
    )

    if (selected) {
      Image(
        drawableId = R.drawable.check,
        modifier = Modifier.size(dimensionResource(R.dimen.icon_small_size))
      )
    }
  }

@Composable
private fun WiFiListDialogScope.Buttons(state: WiFiListDialogState) =
  DialogButtonsRow {
    OutlinedButton(
      onClick = { onWiFiListCancel() },
      text = stringResource(id = R.string.cancel),
      modifier = Modifier.weight(1f)
    )
    Button(
      onClick = { onWiFiListSelect() },
      text = stringResource(id = R.string.select),
      modifier = Modifier.weight(1f),
      enabled = state.selected.isNotNull
    )
  }

private val previewScope = object : WiFiListDialogScope {
  override fun onWiFiListDismiss() {}
  override fun onWiFiListCancel() {}
  override fun onWiFiListSelect() {}
  override fun onSsidSelected(ssid: String) {}
  override fun onForceNextClick() {}
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.WiFiListDialog(
      WiFiListDialogState(
        selected = "WiFi 1",
        items = listOf("WiFi 1", "WiFi 2", "SSID 1"),
        warning = true,
        skipAndConnect = true
      )
    )
  }
}

@Preview
@Composable
private fun Preview_Loading() {
  SuplaTheme {
    previewScope.WiFiListDialog(
      WiFiListDialogState()
    )
  }
}

@Preview
@Composable
private fun Preview_Empty() {
  SuplaTheme {
    previewScope.WiFiListDialog(
      WiFiListDialogState(items = emptyList())
    )
  }
}
