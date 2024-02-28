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

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import org.supla.android.core.ui.theme.listItemValue
import org.supla.android.extensions.max

@Composable
fun ListItemValue(value: String, scale: Float) {
  val valueSize = MaterialTheme.typography.listItemValue().fontSize.let { max(it, it.times(scale)) }

  Text(
    text = value,
    style = MaterialTheme.typography.listItemValue().copy(fontSize = valueSize),
    maxLines = 1,
    overflow = TextOverflow.Ellipsis
  )
}
