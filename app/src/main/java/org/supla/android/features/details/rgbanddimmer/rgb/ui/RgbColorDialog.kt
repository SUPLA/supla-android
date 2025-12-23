package org.supla.android.features.details.rgbanddimmer.rgb.ui
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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.toColorOrNull
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogDoubleButtons
import org.supla.android.ui.dialogs.DialogHeader
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle
import org.supla.android.ui.views.forms.TextField
import org.supla.android.ui.views.forms.TextFieldLabel

data class ColorDialogState(
  val color: String = "",
)

interface RgbColorDialogScope {
  fun onColorDialogDismiss()
  fun onColorDialogConfirm()
  fun onColorDialogInputChange(value: String)
  fun onOpenColorDialog()
}

@Composable
fun RgbColorDialogScope.ColorDialog(
  state: ColorDialogState
) {
  val color = state.color.toColorOrNull()
  Dialog(
    onDismiss = { onColorDialogDismiss() }
  ) {
    DialogHeader(stringResource(R.string.rgb_detail_select_color))
    Separator(style = SeparatorStyle.LIGHT)
    TextField(
      value = state.color,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = Distance.small, vertical = Distance.default),
      onValueChange = { onColorDialogInputChange(it) },
      label = { TextFieldLabel(stringResource(R.string.rgb_detail_color)) },
      trailingIcon = { ColorBox(color) }
    )
    Separator(style = SeparatorStyle.LIGHT)
    DialogDoubleButtons(
      onPositiveClick = { onColorDialogConfirm() },
      onNegativeClick = { onColorDialogDismiss() },
      positiveEnabled = color != null
    )
  }
}

private val previewScope = object : RgbColorDialogScope {
  override fun onColorDialogDismiss() {}
  override fun onColorDialogConfirm() {}
  override fun onColorDialogInputChange(value: String) {}
  override fun onOpenColorDialog() {}
}

@Composable
@SuplaPreview
private fun Preview() {
  SuplaTheme {
    Box(
      modifier = Modifier.fillMaxSize()
    ) {
      previewScope.ColorDialog(ColorDialogState("#FFAADD"))
    }
  }
}

@Composable
@SuplaPreview
private fun PreviewMissingColor() {
  SuplaTheme {
    Box(
      modifier = Modifier.fillMaxSize()
    ) {
      previewScope.ColorDialog(ColorDialogState("#FFAAD"))
    }
  }
}
