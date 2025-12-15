package org.supla.android.features.details.rgbanddimmer.common
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.extensions.applyBrightness
import org.supla.android.ui.views.ReorderableRow

interface SavedColorListScope {
  fun onSavedColorSelected(color: SavedColor)
  fun onSaveCurrentColor()
  fun onRemoveColor(positionOnList: Int)
  fun onMoveColors(from: Int, to: Int)
}

@Composable
fun SavedColorListScope.SavedColors(savedColors: List<SavedColor>, online: Boolean) {
  ReorderableRow(
    items = savedColors,
    onRemove = { onRemoveColor(it) },
    onMove = { from, to -> onMoveColors(from, to) },
    leadingContent = { dragging, itemOver ->
      if (online) {
        val color = if (itemOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        SavedColorAction(color, dragging)
      }
    },
    modifier = Modifier
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = Distance.default)
  ) {
    SavedColorBox(it, online = online)
  }
}

@Composable
private fun SavedColorListScope.SavedColorBox(color: SavedColor, online: Boolean) =
  Box(
    modifier = Modifier
      .padding(horizontal = Distance.tiny)
      .width(42.dp)
      .height(36.dp)
      .background(
        color = color.color.applyBrightness(color.brightness),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
      )
      .border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
      )
      .clickable(enabled = online) { onSavedColorSelected(color) }
  )

@Composable
private fun SavedColorListScope.SavedColorAction(color: Color, dragging: Boolean) =
  Box(
    modifier = Modifier
      .padding(end = Distance.tiny)
      .width(42.dp)
      .height(36.dp)
      .border(
        width = 1.dp,
        color = color,
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
      )
      .clickable(enabled = !dragging) {
        onSaveCurrentColor()
      }
  ) {
    Icon(
      painter = painterResource(if (dragging) R.drawable.ic_delete else R.drawable.ic_plus),
      contentDescription = "Usuń",
      tint = color,
      modifier = Modifier
        .align(Alignment.Center)
        .size(if (dragging) 16.dp else 12.dp)
    )
  }
