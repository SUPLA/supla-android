package org.supla.android.main.scaffold
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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

val LocalScaffoldPadding = staticCompositionLocalOf { PaddingValues(0.dp) }

@Composable
fun Modifier.screenUnderTopBarPaddings(): Modifier {
  val scaffoldPadding = LocalScaffoldPadding.current
  val layoutDirection = LocalLayoutDirection.current
  return this.padding(
    start = scaffoldPadding.calculateStartPadding(layoutDirection),
    end = scaffoldPadding.calculateEndPadding(layoutDirection),
    bottom = scaffoldPadding.calculateBottomPadding()
  )
}

@Composable
fun Modifier.screenPaddings(): Modifier {
  return this.padding(LocalScaffoldPadding.current)
}

@Composable
fun Modifier.topSearchBarPaddings(): Modifier {
  val scaffoldPadding = LocalScaffoldPadding.current
  return this.padding(
    top = scaffoldPadding.calculateTopPadding()
  )
}

@Composable
fun PaddingValues.withLeftPanel(): PaddingValues {
  val layoutDirection = LocalLayoutDirection.current

  return PaddingValues(
    start = 0.dp,
    top = calculateTopPadding(),
    end = calculateEndPadding(layoutDirection),
    bottom = calculateBottomPadding()
  )
}
