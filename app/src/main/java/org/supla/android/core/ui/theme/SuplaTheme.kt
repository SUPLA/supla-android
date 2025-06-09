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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders

@Composable
fun SuplaTheme(
  darkMode: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val suplaDarkColors = SuplaDarkColors(LocalContext.current)
  val suplaLightColors = SuplaLightColors(LocalContext.current)
  val colorScheme = if (darkMode) suplaDarkColors.toMaterial() else suplaLightColors.toMaterial()

  MaterialTheme(
    colorScheme = colorScheme,
    typography = suplaTypography(),
    content = {
      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground, content = content)
    }
  )
}

@Composable
fun SuplaGlanceTheme(
  content: @Composable () -> Unit
) {
  val suplaDarkColors = SuplaDarkColors(androidx.glance.LocalContext.current)
  val suplaLightColors = SuplaLightColors(androidx.glance.LocalContext.current)

  GlanceTheme(
    colors = ColorProviders(light = suplaLightColors.toMaterial(), dark = suplaDarkColors.toMaterial()),
    content = content
  )
}
