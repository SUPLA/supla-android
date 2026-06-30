package org.supla.android.features.statedialog
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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogDoubleButtons
import org.supla.android.ui.dialogs.DialogHeader
import org.supla.android.ui.views.forms.Checkbox
import org.supla.android.ui.views.forms.TextField
import org.supla.android.ui.views.forms.TextFieldLabel
import org.supla.android.ui.views.texts.BodyMedium
import org.supla.core.shared.infrastructure.LocalizedString

data class LifespanDialogState(
  val title: LocalizedString,
  val resetActive: Boolean = false,
  val lifespanValue: String = "",
  val saveEnabled: Boolean = false,
  val processing: Boolean = false,
  val error: Boolean = false,

  val lifespanInitialValue: Int = 0,
  val channelId: Int = 0
)

interface LifespanDialogScope {
  fun onLifespanDialogDismiss()
  fun onLifeSpanDialogResetChange(checked: Boolean)
  fun onLifespanDialogValueChange(value: String)
  fun onLifespanDialogOk()
}

@Composable
fun LifespanDialogScope.LifespanDialog(
  state: LifespanDialogState
) {
  Dialog(
    onDismiss = { onLifespanDialogDismiss() }
  ) {
    DialogHeader(state.title())
    Checkbox(
      checked = state.resetActive,
      label = stringResource(R.string.reset_lightsource_usage_counter),
      labelColor = MaterialTheme.colorScheme.onSurface,
      checkmarkColor = MaterialTheme.colorScheme.onPrimary,
      uncheckedColor = MaterialTheme.colorScheme.onSurface,
      checkedColor = MaterialTheme.colorScheme.primary,
      onCheckedChange = { onLifeSpanDialogResetChange(it) },
      modifier = Modifier.padding(horizontal = Distance.default)
    )
    TextField(
      value = state.lifespanValue,
      label = { TextFieldLabel(stringId = R.string.light_source_lifespan_hours) },
      suffix = { BodyMedium(stringRes = R.string.hp_hour, color = MaterialTheme.colorScheme.onSurfaceVariant) },
      modifier = Modifier
        .padding(horizontal = Distance.default, vertical = Distance.small)
        .fillMaxWidth(),
      onValueChange = { onLifespanDialogValueChange(it) },
      keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
    )

    if (state.error) {
      BodyMedium(
        stringRes = R.string.nfc_lock_tag_error_title,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier
          .padding(horizontal = Distance.default)
          .fillMaxWidth()
      )
    }

    DialogDoubleButtons(
      onNegativeClick = { onLifespanDialogDismiss() },
      onPositiveClick = { onLifespanDialogOk() },
      positiveEnabled = state.saveEnabled,
      processing = state.processing
    )
  }
}

val previewScope = object : LifespanDialogScope {
  override fun onLifespanDialogDismiss() {}
  override fun onLifeSpanDialogResetChange(checked: Boolean) {}
  override fun onLifespanDialogValueChange(value: String) {}
  override fun onLifespanDialogOk() {}
}

@SuplaPreview
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.LifespanDialog(
      state = LifespanDialogState(
        title = LocalizedString.Constant("Light relay"),
        error = true
      )
    )
  }
}

@SuplaPreview
@Composable
private fun PreviewSelected() {
  SuplaTheme {
    previewScope.LifespanDialog(
      state = LifespanDialogState(
        title = LocalizedString.Constant("Light relay"),
        resetActive = true,
        lifespanValue = "10000",
        saveEnabled = true
      )
    )
  }
}
