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

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.general.SingleOptionalSelectionList
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.infrastructure.LocalizedString

@Composable
fun <T> Spinner(
  label: String,
  options: Map<T, String>,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  fillMaxWidth: Boolean = false,
  onOptionSelected: (selectedId: T) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val firstOptionText = options[options.keys.first()] ?: ""
  var selectedOptionText by remember(options) { mutableStateOf(firstOptionText) }

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(
      text = label.uppercase(),
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.padding(horizontal = 12.dp),
      color = colorResource(id = R.color.on_surface_variant)
    )
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { ifTrue(enabled) { expanded = it } }) {
      SpinnerTextField(selectedOptionText, expanded, enabled, fillMaxWidth)
      ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { option ->
          DropdownMenuItem(
            text = { Text(option.value, style = MaterialTheme.typography.bodyLarge) },
            onClick = {
              onOptionSelected(option.key)
              selectedOptionText = option.value
              expanded = false
            }
          )
        }
      }
    }
  }
}

interface SpinnerItem {
  val label: LocalizedString
}

@Composable
fun <T : SpinnerItem> Spinner(
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
    options.label?.let {
      Text(
        text = stringResource(id = it).uppercase(),
        style = MaterialTheme.typography.bodySmall,
        color = labelTextColor,
        modifier = Modifier.padding(horizontal = 12.dp),
      )
    }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { ifTrue(enabled) { expanded = it } }) {
      SpinnerTextField(selectedOptionText ?: "---", expanded, enabled, fillMaxWidth, options.isError)
      ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.items.forEach { option ->
          DropdownMenuItem(
            text = {
              Text(
                text = option.label(LocalContext.current),
                style = MaterialTheme.typography.bodyLarge
              )
            },
            onClick = {
              onOptionSelected(option)
              expanded = false
            }
          )
        }
      }
    }
  }
}

@Composable
fun <T : SpinnerItem> TextSpinner(
  options: SingleSelectionList<T>,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  active: Boolean = true,
  labelTextColor: Color = colorResource(id = R.color.on_surface_variant),
  labelAlignment: Alignment.Horizontal = Alignment.Start,
  labelPadding: PaddingValues = PaddingValues(0.dp),
  onOptionSelected: (selectedId: T) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedOptionText = options.selected.label(LocalContext.current)

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    options.label?.let {
      Text(
        text = stringResource(id = it).uppercase(),
        style = MaterialTheme.typography.bodySmall,
        color = labelTextColor,
        modifier = Modifier
          .align(labelAlignment)
          .padding(labelPadding),
        maxLines = 1
      )
    }
    ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { ifTrue(enabled && active) { expanded = it } },
      modifier = Modifier.height(24.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = selectedOptionText,
          style = MaterialTheme.typography.bodySmall,
          color = if (enabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline,
          modifier = Modifier
            .menuAnchor(MenuAnchorType.PrimaryEditable)
            .weight(1f, false),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        SpinnerTrailingIcon(
          expanded = expanded,
          enabled = enabled && active,
          modifier = Modifier
            .width(16.dp)
            .height(8.dp)
            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
      }

      // Height limitation is needed for Android 9.
      // There is some bug which causes that top list elements are not visible.
      val limitHeightModifier: Modifier =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
          Modifier.heightIn(max = 300.dp)
        } else {
          Modifier
        }

      DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = limitHeightModifier
      ) {
        options.items.forEach { option ->
          DropdownMenuItem(
            text = {
              Text(
                text = option.label(LocalContext.current),
                style = MaterialTheme.typography.bodyMedium
              )
            },
            onClick = {
              onOptionSelected(option)
              expanded = false
            }
          )
        }
      }
    }
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
      Spinner(label = "Program", options = mapOf(1 to "Cooling", 2 to "Heating"), onOptionSelected = {})
      TextSpinner(options = SingleSelectionList(ChartRange.DAY, emptyList(), R.string.history_range_label), onOptionSelected = {})
    }
  }
}
