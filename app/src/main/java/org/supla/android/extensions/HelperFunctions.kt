package org.supla.android.extensions
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

import android.annotation.SuppressLint
import androidx.annotation.DimenRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

fun max(a: TextUnit, b: TextUnit): TextUnit {
  return if (a.value < b.value) b else a
}

@Composable
@ReadOnlyComposable
@SuppressLint("LocalContextConfigurationRead")
fun fontDimensionResource(@DimenRes id: Int): TextUnit {
  val density = LocalDensity.current.density
  val resources = LocalContext.current.resources
  val fontScale = resources.configuration.fontScale
  val dimension = resources.getDimension(id)

  return dimension.div(density).div(fontScale).sp
}

@Composable
@ReadOnlyComposable
@SuppressLint("LocalContextConfigurationRead")
fun fontDpSize(dp: Dp): TextUnit {
  val density = LocalDensity.current.density
  val resources = LocalContext.current.resources
  val fontScale = resources.configuration.fontScale
  val dimension = dp.toPx()

  return dimension.div(density).div(fontScale).sp
}
