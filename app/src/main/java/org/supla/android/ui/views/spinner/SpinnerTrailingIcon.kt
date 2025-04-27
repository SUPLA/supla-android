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

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import org.supla.android.R

@Composable
fun SpinnerTrailingIcon(expanded: Boolean, enabled: Boolean, modifier: Modifier = Modifier) =
  IconButton(modifier = Modifier.clearAndSetSemantics { }, onClick = { }) {
    Icon(
      painter = painterResource(id = R.drawable.ic_dropdown),
      contentDescription = null,
      tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
      modifier = modifier.rotate(
        if (expanded) {
          180f
        } else {
          360f
        }
      )
    )
  }
