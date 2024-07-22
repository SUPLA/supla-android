package org.supla.android.core.ui.theme
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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun SuplaTheme(
  darkMode: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val suplaDarkColors = SuplaDarkColors(LocalContext.current)
  val suplaLightColors = SuplaLightColors(LocalContext.current)
  val colors = if (darkMode) suplaDarkColors.toMaterial() else suplaLightColors.toMaterial()
  val colorScheme = if (darkMode) suplaDarkColors.toMaterial3() else suplaLightColors.toMaterial3()

  androidx.compose.material.MaterialTheme(
    colors = colors,
    typography = SuplaTypographyMaterial2(colors),
    content = {
      androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        content = content,
        typography = suplaTypographyMaterial3()
      )
    }
  )
}
