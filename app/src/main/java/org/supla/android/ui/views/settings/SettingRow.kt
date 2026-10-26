package org.supla.android.ui.views.settings
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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import org.supla.android.core.ui.theme.Distance

@Composable
fun SettingsColumnScope.SettingRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
  val bottomLineColor = MaterialTheme.colorScheme.background
  val startDistance = Distance.small

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .drawBehind {
        if (index < count - 1) {
          val strokeWidth = 1.5.dp.toPx()
          val y = size.height - strokeWidth

          drawLine(
            color = bottomLineColor,
            start = Offset(startDistance.toPx(), y),
            end = Offset(size.width, y),
            strokeWidth = strokeWidth
          )
        }
      }
      .padding(horizontal = startDistance, vertical = 4.dp),
    content = content
  )
}
