package org.supla.android.main.view
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.supla.android.R

@Composable
fun ChannelListLabel() = NavigationBarLabel(R.string.navbar_channels)

@Composable
fun GroupListLabel() = NavigationBarLabel(R.string.navbar_groups)

@Composable
fun SceneListLabel() = NavigationBarLabel(R.string.navbar_scenes)

@Composable
fun NavigationBarLabel(@StringRes stringRes: Int) =
  Text(
    text = stringResource(stringRes),
    style = MaterialTheme.typography.labelSmall
  )
