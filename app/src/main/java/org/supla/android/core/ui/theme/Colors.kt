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

import android.content.Context
import android.os.Build
import androidx.annotation.ColorRes
import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.supla.android.R

class SuplaLightColors(private val context: Context) {
  fun toMaterial() = lightColors(
    primary = colorResource(context, R.color.primary),
    primaryVariant = colorResource(context, R.color.primary_variant),
    onPrimary = colorResource(context, R.color.on_primary),
    background = colorResource(context, R.color.background),
    onBackground = colorResource(context, R.color.on_background),
    surface = colorResource(context, R.color.surface),
    onSurface = colorResource(context, R.color.on_surface),
    error = colorResource(context, R.color.error),
    onError = Color(0xFFFFFFFF)
  )

  fun toMaterial3() = ColorScheme(
    primary = colorResource(context, R.color.primary),
    onPrimary = colorResource(context, R.color.on_primary),
    primaryContainer = colorResource(context, R.color.primary_container),
    onPrimaryContainer = colorResource(context, R.color.on_primary_container),
    inversePrimary = colorResource(context, R.color.primary),
    secondary = colorResource(context, R.color.secondary),
    onSecondary = colorResource(context, R.color.on_primary),
    secondaryContainer = colorResource(context, R.color.secondary_container),
    onSecondaryContainer = colorResource(context, R.color.on_surface),
    tertiary = colorResource(context, R.color.primary_variant),
    onTertiary = colorResource(context, R.color.on_primary),
    tertiaryContainer = colorResource(context, R.color.surface),
    onTertiaryContainer = colorResource(context, R.color.on_surface),
    background = colorResource(context, R.color.background),
    onBackground = colorResource(context, R.color.on_background),
    surface = colorResource(context, R.color.surface),
    onSurface = colorResource(context, R.color.on_surface),
    surfaceVariant = colorResource(context, R.color.surface_variant),
    onSurfaceVariant = colorResource(context, R.color.on_surface_variant),
    surfaceTint = colorResource(context, R.color.on_surface),
    inverseSurface = colorResource(context, R.color.surface),
    inverseOnSurface = colorResource(context, R.color.on_surface),
    error = colorResource(context, R.color.error),
    onError = Color(0xFFFFFFFF),
    errorContainer = colorResource(context, R.color.error_container),
    onErrorContainer = Color(0xFFFFFFFF),
    outline = colorResource(context, R.color.outline),
    outlineVariant = colorResource(context, R.color.outline_variant),
    scrim = colorResource(context, R.color.dialog_scrim)
  )
}

class SuplaDarkColors(private val context: Context) {
  fun toMaterial() = darkColors(
    primary = colorResource(context, R.color.primary),
    primaryVariant = colorResource(context, R.color.primary_variant),
    onPrimary = colorResource(context, R.color.on_primary),
    background = colorResource(context, R.color.background),
    onBackground = colorResource(context, R.color.on_background),
    surface = colorResource(context, R.color.surface),
    onSurface = colorResource(context, R.color.on_surface),
    error = colorResource(context, R.color.error),
    onError = Color(0xFFF5F6F7)
  )

  fun toMaterial3() = ColorScheme(
    primary = colorResource(context, R.color.primary),
    onPrimary = colorResource(context, R.color.on_primary),
    primaryContainer = colorResource(context, R.color.primary_container),
    onPrimaryContainer = colorResource(context, R.color.on_primary_container),
    inversePrimary = colorResource(context, R.color.primary),
    secondary = colorResource(context, R.color.secondary),
    onSecondary = colorResource(context, R.color.on_primary),
    secondaryContainer = colorResource(context, R.color.secondary_container),
    onSecondaryContainer = colorResource(context, R.color.on_surface),
    tertiary = colorResource(context, R.color.primary_variant),
    onTertiary = colorResource(context, R.color.on_primary),
    tertiaryContainer = colorResource(context, R.color.surface),
    onTertiaryContainer = colorResource(context, R.color.on_surface),
    background = colorResource(context, R.color.background),
    onBackground = colorResource(context, R.color.on_background),
    surface = colorResource(context, R.color.surface),
    onSurface = colorResource(context, R.color.on_surface),
    surfaceVariant = colorResource(context, R.color.surface_variant),
    onSurfaceVariant = colorResource(context, R.color.on_surface_variant),
    surfaceTint = colorResource(context, R.color.on_surface),
    inverseSurface = colorResource(context, R.color.surface),
    inverseOnSurface = colorResource(context, R.color.on_surface),
    error = colorResource(context, R.color.error),
    onError = Color(0xFFF5F6F7),
    errorContainer = colorResource(context, R.color.error_container),
    onErrorContainer = Color(0xFFF5F6F7),
    outline = colorResource(context, R.color.outline),
    outlineVariant = colorResource(context, R.color.outline_variant),
    scrim = colorResource(context, R.color.dialog_scrim)
  )
}

val ColorScheme.progressPointShadow: Color
  get() = Color(0x99B2F4B8)

val ColorScheme.primaryVariant: Color
  @Composable
  get() = androidx.compose.ui.res.colorResource(R.color.primary_variant)

val Colors.gray: Color
  get() = Color(0xFF7E8082)

val ColorScheme.gray: Color
  get() = Color(0xFF7E8082)

private fun colorResource(context: Context, @ColorRes id: Int): Color {
  return if (Build.VERSION.SDK_INT >= 23) {
    Color(context.resources.getColor(id, context.theme))
  } else {
    @Suppress("DEPRECATION")
    Color(context.resources.getColor(id))
  }
}
