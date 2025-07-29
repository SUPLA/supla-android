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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.addwizard.AddWizardScope
import org.supla.android.features.addwizard.model.AddWizardScreen
import org.supla.android.features.addwizard.view.components.AddWizardContentText
import org.supla.android.features.addwizard.view.components.AddWizardScaffold
import kotlin.time.Duration.Companion.milliseconds

interface AddWizardConfigurationScope : AddWizardScope

@Composable
fun AddWizardConfigurationScope.AddWizardConfigurationView(
  processing: Boolean
) {
  AddWizardScaffold(
    iconRes = R.drawable.add_wizard_step_3,
    buttonTextId = R.string.start,
    processing = processing,
    onNext = { onStepFinished(AddWizardScreen.Configuration) },
  ) {
    AddWizardContentText(R.string.add_wizard_step_3_message_1)
    BlinkingDot()
    AddWizardContentText(R.string.add_wizard_step_3_message_2)
    AddWizardContentText(R.string.add_wizard_step_3_message_3)
  }
}

@Composable
private fun BlinkingDot() =
  Box(modifier = Modifier.fillMaxWidth().height(32.dp)) {
    var dotVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Any()) {
      while (true) {
        delay(100.milliseconds)
        dotVisible = !dotVisible
      }
    }

    if (dotVisible) {
      Box(
        modifier = Modifier
          .size(20.dp)
          .background(Color(0xFF50F949), shape = CircleShape)
          .align(Alignment.CenterEnd)
      )
    }
  }

private val previewScope = object : AddWizardConfigurationScope {
  override fun onStepFinished(step: AddWizardScreen) {}
  override fun onClose(step: AddWizardScreen) {}
}

@Preview(backgroundColor = 0xFF12A71E, showBackground = true)
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.AddWizardConfigurationView(false)
  }
}
