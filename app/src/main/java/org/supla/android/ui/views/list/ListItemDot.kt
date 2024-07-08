package org.supla.android.ui.views.list
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.lists.ListOnlineState

@Composable
fun ListItemDot(onlineState: ListOnlineState, withButton: Boolean, paddingValues: PaddingValues, modifier: Modifier = Modifier) {
  val onlineColor = colorResource(id = R.color.primary)
  val offlineColor = colorResource(id = R.color.red)
  val color = if (onlineState.online) onlineColor else offlineColor
  val size = dimensionResource(id = R.dimen.channel_dot_size)
  val background = if (withButton) color else Color.Transparent
  if (onlineState == ListOnlineState.PARTIALLY_ONLINE && withButton) {
    val radius = size.div(2)
    val topShape = RoundedCornerShape(topStart = radius, topEnd = radius)
    Box(
      modifier = Modifier
        .padding(paddingValues = paddingValues)
        .size(size)
    ) {
      Box(
        modifier = modifier
          .width(size)
          .height(radius)
          .border(width = 1.dp, color = onlineColor, shape = topShape)
          .background(color = onlineColor, shape = topShape)
      )
      val bottomShape = RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
      Box(
        modifier = modifier
          .padding(top = radius)
          .width(size)
          .height(radius)
          .border(width = 1.dp, color = offlineColor, shape = bottomShape)
          .background(color = offlineColor, shape = bottomShape)
      )
    }
  } else {
    Box(
      modifier = modifier
        .padding(paddingValues = paddingValues)
        .size(size)
        .border(width = 1.dp, color = color, shape = CircleShape)
        .background(color = background, shape = CircleShape)
    )
  }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
  SuplaTheme {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(10.dp)) {
      ListItemDot(onlineState = ListOnlineState.ONLINE, withButton = true, paddingValues = PaddingValues(0.dp))
      ListItemDot(onlineState = ListOnlineState.ONLINE, withButton = false, paddingValues = PaddingValues(0.dp))
      ListItemDot(onlineState = ListOnlineState.OFFLINE, withButton = true, paddingValues = PaddingValues(0.dp))
      ListItemDot(onlineState = ListOnlineState.OFFLINE, withButton = false, paddingValues = PaddingValues(0.dp))
      ListItemDot(onlineState = ListOnlineState.PARTIALLY_ONLINE, withButton = true, paddingValues = PaddingValues(0.dp))
      ListItemDot(onlineState = ListOnlineState.PARTIALLY_ONLINE, withButton = false, paddingValues = PaddingValues(0.dp))
    }
  }
}
