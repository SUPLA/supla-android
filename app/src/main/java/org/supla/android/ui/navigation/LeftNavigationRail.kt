package org.supla.android.ui.navigation
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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import org.supla.android.R

@Composable
fun LeftNavigationRail(
  content: @Composable ColumnScope.() -> Unit
) =
  NavigationRail(
    modifier = Modifier
      .fillMaxHeight()
      .border(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(
      modifier = Modifier.fillMaxHeight(),
    ) {
      Spacer(modifier = Modifier.height(dimensionResource(R.dimen.top_bar_height)))

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.SpaceEvenly
      ) {
        content()
      }
    }
  }
