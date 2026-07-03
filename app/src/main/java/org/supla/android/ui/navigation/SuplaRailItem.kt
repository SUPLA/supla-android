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

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import org.supla.android.core.storage.LocalApplicationPreferences

@Composable
fun SuplaRailItem(
  selected: Boolean,
  onClick: () -> Unit,
  @DrawableRes iconRes: Int,
  label: @Composable () -> Unit,
  iconDescription: String? = null
) =
  SuplaRailItem(
    selected = selected,
    onClick = onClick,
    icon = { Icon(painter = painterResource(iconRes), iconDescription) },
    label = label,
  )

@Composable
fun SuplaRailItem(
  selected: Boolean,
  onClick: () -> Unit,
  icon: @Composable () -> Unit,
  label: @Composable () -> Unit
) =
  NavigationRailItem(
    selected = selected,
    onClick = onClick,
    icon = icon,
    label = if (LocalApplicationPreferences.current.isShowBottomLabel) label else null,
    colors = NavigationRailItemDefaults.colors(
      selectedIconColor = MaterialTheme.colorScheme.primary,
      selectedTextColor = MaterialTheme.colorScheme.primary,
      indicatorColor = Color.Transparent,
      unselectedIconColor = MaterialTheme.colorScheme.onBackground,
      unselectedTextColor = MaterialTheme.colorScheme.onBackground
    )
  )
