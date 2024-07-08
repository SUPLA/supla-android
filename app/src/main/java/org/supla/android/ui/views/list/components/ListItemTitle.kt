package org.supla.android.ui.views.list.components
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

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import org.supla.android.core.ui.theme.listItemCaption

@Composable
fun ListItemTitle(
  text: String,
  onLongClick: () -> Unit,
  onItemClick: () -> Unit,
  modifier: Modifier = Modifier,
  scale: Float = 1f
) {
  val textSize = MaterialTheme.typography.listItemCaption().fontSize.times(java.lang.Float.max(scale, 1f))
  Text(
    text = text,
    style = MaterialTheme.typography.listItemCaption(),
    modifier = modifier
      .pointerInput(onLongClick, onItemClick) {
        detectTapGestures(onLongPress = { onLongClick() }, onTap = { onItemClick() })
      },
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    fontSize = textSize
  )
}
