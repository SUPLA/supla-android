package org.supla.android.features.addwizard.view.components
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme

data class DeviceParameter(
  @StringRes val nameRes: Int,
  val value: String
)

@Composable
fun DeviceParametersTable(
  rowData: List<DeviceParameter>,
  modifier: Modifier = Modifier,
  columnSpacing: Dp = Distance.tiny,
  rowSpacing: Dp = 0.dp,
  firstColumnTextStyle: TextStyle = MaterialTheme.typography.labelSmall,
  secondColumnTextStyle: TextStyle = MaterialTheme.typography.bodySmall
) {
  val density = LocalDensity.current

  SubcomposeLayout(modifier = modifier) { constraints ->
    var firstColumnWidth = 0

    rowData.forEachIndexed { index, row ->
      val measurables = subcompose(slotId = "firstColText-$index") {
        Text(
          text = stringResource(row.nameRes),
          style = firstColumnTextStyle
        )
      }
      val placeable = measurables.first().measure(constraints.copy(minWidth = 0))
      firstColumnWidth = maxOf(firstColumnWidth, placeable.width)
    }

    val firstColumnWidthDp = with(density) { firstColumnWidth.toDp() }

    val placeables = mutableListOf<Placeable>()
    var tableHeight = 0

    rowData.forEachIndexed { index, row ->
      val measurables = subcompose(slotId = "fullRow-$index") {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(columnSpacing),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = stringResource(row.nameRes),
            style = firstColumnTextStyle,
            textAlign = TextAlign.End,
            modifier = Modifier.width(firstColumnWidthDp)
          )
          Text(
            text = row.value,
            style = secondColumnTextStyle,
            modifier = Modifier.weight(1f)
          )
        }
      }
      val placeable = measurables.first().measure(constraints)
      placeables.add(placeable)
      tableHeight += placeable.height
    }

    tableHeight += (rowData.size - 1).coerceAtLeast(0) * rowSpacing.roundToPx()

    layout(constraints.maxWidth, tableHeight) {
      var currentYPosition = 0
      placeables.forEach { placeable ->
        placeable.placeRelative(x = 0, y = currentYPosition)
        currentYPosition += placeable.height + rowSpacing.roundToPx()
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewTwoColumnTable() {
  SuplaTheme {
    val data = listOf(
      DeviceParameter(R.string.wizard_iodev_name, "My Device 123"),
      DeviceParameter(R.string.wizard_iodev_firmware, "v1.0.2 B20240624")
    )
    DeviceParametersTable(
      rowData = data,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .background(Color.LightGray.copy(alpha = 0.2f))
    )
  }
}
