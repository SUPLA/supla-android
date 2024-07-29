@file:OptIn(ExperimentalMaterial3Api::class)

package org.supla.android.ui.views
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme

@Composable
fun <T> Spinner(label: String, options: Map<T, String>, modifier: Modifier = Modifier, onOptionSelected: (selectedId: T) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  val firstOptionText = options[options.keys.first()] ?: ""
  var selectedOptionText by remember { mutableStateOf(firstOptionText) }

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(
      text = label.uppercase(),
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.padding(horizontal = 12.dp),
      color = colorResource(id = R.color.gray)
    )
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
      TextField(
        value = selectedOptionText,
        readOnly = true,
        trailingIcon = { SpinnerTrailingIcon(expanded = expanded, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)) },
        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)
      )
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

@Composable
fun <T> TextSpinner(
  label: String,
  options: Map<T, String>,
  modifier: Modifier = Modifier,
  selectedOption: T? = null,
  onOptionSelected: (selectedId: T) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedOptionText = options[selectedOption ?: options.keys.firstOrNull()] ?: ""

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(
      text = label.uppercase(),
      style = MaterialTheme.typography.bodySmall,
      color = colorResource(id = R.color.gray)
    )
    ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = it },
      modifier = Modifier.height(24.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = selectedOptionText,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onBackground,
          modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)
        )
        SpinnerTrailingIcon(
          expanded = expanded,
          modifier = Modifier
            .width(16.dp)
            .height(8.dp)
            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
      }
      DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { option ->
          DropdownMenuItem(
            text = { Text(option.value, style = MaterialTheme.typography.bodyMedium) },
            onClick = {
              onOptionSelected(option.key)
              expanded = false
            }
          )
        }
      }
    }
  }
}

@Composable
private fun SpinnerTrailingIcon(expanded: Boolean, modifier: Modifier = Modifier) =
  IconButton(modifier = Modifier.clearAndSetSemantics { }, onClick = { }) {
    Icon(
      painter = painterResource(id = R.drawable.ic_dropdown),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onBackground,
      modifier = modifier.rotate(
        if (expanded) {
          180f
        } else {
          360f
        }
      )
    )
  }

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
      Spinner(label = "Program", options = mapOf(1 to "Cooling", 2 to "Heating"), onOptionSelected = {})
      TextSpinner(label = "Program", options = mapOf(1 to "Cooling", 2 to "Heating very long"), onOptionSelected = {})
    }
  }
}
