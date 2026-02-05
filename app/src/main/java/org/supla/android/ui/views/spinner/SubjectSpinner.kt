@file:OptIn(ExperimentalMaterial3Api::class)

package org.supla.android.ui.views.spinner
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

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.data.model.general.SingleOptionalSelectionList
import org.supla.android.images.ImageId
import org.supla.android.ui.views.Image
import org.supla.core.shared.extensions.ifTrue

interface SubjectSpinnerItem : SpinnerItem {
  val icon: ImageId?
  val isLocation: Boolean

  @Composable
  fun background(): Color =
    if (isLocation) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
}

@Composable
fun <T : SubjectSpinnerItem> LabelledSpinner(
  options: SingleOptionalSelectionList<T>,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  fillMaxWidth: Boolean = false,
  labelTextColor: Color = colorResource(id = R.color.on_surface_variant),
  onOptionSelected: (selected: T) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedOptionText = options.selected?.label(LocalContext.current)

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    options.label?.let { SpinnerLabel(it, labelTextColor) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { ifTrue(enabled) { expanded = it } }) {
      SpinnerTextField(selectedOptionText ?: "---", expanded, enabled, fillMaxWidth)
      ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.items.forEach { option ->
          DropdownMenuItem(
            text = { SpinnerListItem(option) },
            enabled = option.isLocation.not(),
            onClick = {
              onOptionSelected(option)
              expanded = false
            },
            modifier = Modifier.background(option.background()),
          )
        }
      }
    }
  }
}

@Composable
private fun SpinnerLabel(@StringRes labelRes: Int, labelColor: Color) =
  Text(
    text = stringResource(id = labelRes).uppercase(),
    style = MaterialTheme.typography.bodySmall,
    color = labelColor,
    modifier = Modifier.padding(horizontal = 12.dp),
  )

@Composable
private fun SpinnerListItem(item: SubjectSpinnerItem) {
  if (item.isLocation) {
    SpinnerListLabelItem(item)
  } else {
    SpinnerListSubjectItem(item)
  }
}

@Composable
private fun SpinnerListLabelItem(item: SubjectSpinnerItem) =
  Text(
    text = item.label(LocalContext.current),
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurface
  )

@Composable
private fun SpinnerListSubjectItem(item: SubjectSpinnerItem) =
  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Distance.tiny)) {
    item.icon?.let {
      Image(it, modifier = Modifier.size(dimensionResource(R.dimen.icon_default_size)))
    }
    Text(
      text = item.label(LocalContext.current),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
