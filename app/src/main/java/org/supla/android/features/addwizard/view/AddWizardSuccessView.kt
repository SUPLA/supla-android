package org.supla.android.features.addwizard.view
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.addwizard.AddWizardScope
import org.supla.android.features.addwizard.model.AddWizardScreen
import org.supla.android.features.addwizard.view.components.AddWizardContentText
import org.supla.android.features.addwizard.view.components.AddWizardScaffold
import org.supla.android.features.addwizard.view.components.DeviceParameter
import org.supla.android.features.addwizard.view.components.DeviceParametersTable
import org.supla.android.features.addwizard.view.components.addWizardButtonColors
import org.supla.android.ui.views.buttons.OutlinedButton

interface AddWizardSuccessScope : AddWizardScope {
  fun onAgain()
}

@Composable
fun AddWizardSuccessScope.AddWizardSuccessView(deviceParameters: List<DeviceParameter>) {
  AddWizardScaffold(
    iconRes = R.drawable.add_wizard_success,
    buttonTextId = R.string.exit,
    onNext = { onStepFinished(AddWizardScreen.Success) },
    onClose = { onClose(AddWizardScreen.Success) }
  ) {
    AddWizardContentText(R.string.wizard_done)
    Column(
      modifier = Modifier
        .background(color = MaterialTheme.colorScheme.surface)
        .fillMaxWidth()
        .padding(Distance.tiny),
      verticalArrangement = Arrangement.spacedBy(Distance.tiny)
    ) {
      Text(
        text = stringResource(R.string.wizard_iodev_data),
        style = MaterialTheme.typography.bodyMedium
      )

      DeviceParametersTable(
        rowData = deviceParameters
      )
    }

    AddWizardContentText(R.string.wizard_done_explentations)

    Spacer(modifier = Modifier.weight(1f))
    BackButton()
    Spacer(modifier = Modifier.weight(1f))
  }
}

@Composable
private fun AddWizardSuccessScope.BackButton(modifier: Modifier = Modifier) =
  OutlinedButton(
    colors = ButtonDefaults.addWizardButtonColors(),
    contentPadding = PaddingValues(start = Distance.small, top = Distance.small, end = Distance.small, bottom = Distance.small),
    onClick = { onAgain() },
    modifier = modifier.defaultMinSize(minWidth = 130.dp, minHeight = 56.dp),
  ) {
    androidx.compose.foundation.Image(
      imageVector = Icons.Default.Replay,
      contentDescription = null,
      colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
    )
    Spacer(modifier = Modifier.width(Distance.tiny))
    Text(
      text = "Dodaj kolejne",
      style = MaterialTheme.typography.labelLarge,
    )
  }

private val previewScope = object : AddWizardSuccessScope {
  override fun onStepFinished(step: AddWizardScreen) {}
  override fun onClose(step: AddWizardScreen) {}
  override fun onAgain() {}
}

@Preview(backgroundColor = 0xFF12A71E, showBackground = true)
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.AddWizardSuccessView(
      mutableListOf<DeviceParameter>().apply {
        add(DeviceParameter(R.string.wizard_iodev_name, "deviceName"))
        add(DeviceParameter(R.string.wizard_iodev_firmware, "deviceFirmware"))
        add(DeviceParameter(R.string.wizard_iodev_mac, "deviceMacAddress"))
        add(DeviceParameter(R.string.wizard_iodev_laststate, "lastState"))
      }
    )
  }
}
