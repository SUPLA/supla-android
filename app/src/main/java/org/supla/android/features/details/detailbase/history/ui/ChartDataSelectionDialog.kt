package org.supla.android.features.details.detailbase.history.ui
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

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.supla.android.R
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.general.MultipleSelectionList
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.features.details.electricitymeterdetail.history.ElectricityMeterChartType
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogButtonsRow
import org.supla.android.ui.dialogs.DialogHeader
import org.supla.android.ui.views.Checkbox
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.spinner.SpinnerItem
import org.supla.android.ui.views.spinner.TextSpinner
import org.supla.android.usecases.channel.measurementsprovider.electricity.PhaseItem

@Immutable
data class ChartDataSelectionDialogState(
  val channelName: StringProvider,
  val spinner: SingleSelectionList<SpinnerItem>? = null,
  val checkbox: MultipleSelectionList<CheckboxItem>? = null,
  val checkboxSelector: ((SpinnerItem, Set<CheckboxItem>?) -> MultipleSelectionList<CheckboxItem>?)? = null
)

interface CheckboxItem {
  val color: Int
  val label: Int // StringRes
}

@Composable
fun ChartDataSelectionDialog(
  state: ChartDataSelectionDialogState,
  onPositiveClick: (spinnerItem: SpinnerItem?, checkboxItems: Set<CheckboxItem>?) -> Unit = { _, _ -> },
  onDismiss: () -> Unit = {}
) {
  val localState = remember(state) { mutableStateOf(state) }

  Dialog(onDismiss = onDismiss) {
    DialogHeader(title = state.channelName(LocalContext.current))
    Separator(style = SeparatorStyle.LIGHT)
    Content(localState = localState)
    Separator(style = SeparatorStyle.LIGHT, modifier = Modifier.padding(top = Distance.default))
    Buttons(
      onPositiveClick = { onPositiveClick(localState.value.spinner?.selected, localState.value.checkbox?.selected) },
      onNegativeClick = onDismiss
    )
  }
}

@Composable
private fun Content(localState: MutableState<ChartDataSelectionDialogState>) {
  localState.value.spinner?.let { ContentSpinner(list = it, localState = localState) }
  localState.value.checkbox?.let { ContentCheckBoxes(list = it, localState = localState) }
}

@Composable
private fun ContentSpinner(list: SingleSelectionList<SpinnerItem>, localState: MutableState<ChartDataSelectionDialogState>) =
  TextSpinner(
    options = list,
    enabled = list.items.size > 1,
    modifier = Modifier.padding(start = Distance.default, end = Distance.default, top = Distance.default)
  ) {
    val newCheckbox =
      when (val selector = localState.value.checkboxSelector) {
        null -> localState.value.checkbox
        else -> selector(it, localState.value.checkbox?.selected)
      }
    localState.value = localState.value.copy(spinner = list.copy(selected = it), checkbox = newCheckbox)
  }

@Composable
private fun ContentCheckBoxes(list: MultipleSelectionList<CheckboxItem>, localState: MutableState<ChartDataSelectionDialogState>) {
  if (list.items.count() > 1) {
    list.label?.let {
      Text(
        text = stringResource(id = it).uppercase(),
        style = MaterialTheme.typography.bodySmall,
        color = colorResource(id = R.color.on_surface_variant),
        modifier = Modifier.padding(start = Distance.default, top = Distance.default, end = Distance.default)
      )
    }
    FlowRow(modifier = Modifier.padding(start = Distance.tiny, end = Distance.default)) {
      list.items.forEach { item ->
        Checkbox(
          checked = list.selected.contains(item),
          enabled = !list.disabled.contains(item),
          checkedColor = colorResource(id = item.color),
          uncheckedColor = colorResource(id = item.color),
          checkmarkColor = Color.White,
          label = stringResource(id = item.label),
          onCheckedChange = {
            localState.value = if (it) {
              localState.value.copy(checkbox = localState.value.checkbox?.select(item))
            } else {
              localState.value.copy(checkbox = localState.value.checkbox?.unselect(item))
            }
          }
        )
      }
    }
  }
}

@Composable
private fun Buttons(onPositiveClick: () -> Unit, onNegativeClick: () -> Unit) =
  DialogButtonsRow {
    OutlinedButton(
      onClick = onNegativeClick,
      text = stringResource(id = R.string.cancel),
      modifier = Modifier.weight(1f)
    )
    Button(
      onClick = onPositiveClick,
      text = stringResource(id = R.string.ok),
      modifier = Modifier.weight(1f)
    )
  }

@Composable
@Preview
private fun Preview() {
  SuplaTheme {
    ChartDataSelectionDialog(
      state = ChartDataSelectionDialogState(
        channelName = { "EM" },
        spinner = SingleSelectionList(
          ElectricityMeterChartType.FORWARDED_ACTIVE_ENERGY,
          ElectricityMeterChartType.entries,
          R.string.details_em_chart_data_type
        ),
        checkbox = MultipleSelectionList(
          setOf(PhaseItem.PHASE_1),
          PhaseItem.entries.toSet(),
          R.string.details_em_phases,
          setOf(PhaseItem.PHASE_3)
        )
      ),
    )
  }
}
