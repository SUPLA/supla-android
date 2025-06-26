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

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.addwizard.AddWizardScope
import org.supla.android.features.addwizard.model.AddWizardScreen
import org.supla.android.features.addwizard.view.components.AddWizardContentText
import org.supla.android.features.addwizard.view.components.AddWizardScaffold
import org.supla.core.shared.infrastructure.localizedString

interface AddWizardMessageScope : AddWizardScope

@Composable
fun AddWizardMessageScope.AddWizardMessageView(
  step: AddWizardScreen.Message
) {
  AddWizardScaffold(
    step.iconRes,
    buttonTextId = R.string.exit,
    onNext = { onStepFinished(step) },
    onClose = { onClose(step) }
  ) {
    AddWizardContentText(step.message(LocalContext.current))
  }
}

private val previewScope = object : AddWizardMessageScope {
  override fun onStepFinished(step: AddWizardScreen) {}
  override fun onClose(step: AddWizardScreen) {}
}

@Preview(backgroundColor = 0xFF12A71E, showBackground = true)
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.AddWizardMessageView(AddWizardScreen.Message(R.drawable.wizard_error, localizedString(R.string.wizard_iodevice_notfound)))
  }
}
