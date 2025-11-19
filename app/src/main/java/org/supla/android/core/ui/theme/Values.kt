package org.supla.android.core.ui.theme/*
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

import android.content.res.Configuration
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import org.supla.android.R

object Distance {

  val default: Dp
    @Composable get() = dimensionResource(id = R.dimen.distance_default)

  val small: Dp
    @Composable get() = dimensionResource(id = R.dimen.distance_small)

  val tiny: Dp
    @Composable get() = dimensionResource(id = R.dimen.distance_tiny)

  val horizontal: Dp
    @Composable get() {
      val isTablet = currentWindowAdaptiveInfo().windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
      return if (isTablet) 36.dp else 24.dp
    }

  val vertical: Dp
    @Composable get() {
      val isTablet = currentWindowAdaptiveInfo().windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

      return if (isTablet) {
        36.dp
      } else if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
        24.dp
      } else {
        16.dp
      }
    }

  @Composable
  fun toStatic(): Static = Static(default, small, tiny)

  data class Static(
    val default: Dp,
    val small: Dp,
    val tiny: Dp
  )
}

object Shadow {
  val elevation: Dp
    get() = 4.dp
}
