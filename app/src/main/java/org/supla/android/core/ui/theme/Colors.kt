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
import androidx.compose.ui.platform.LocalContext
import org.supla.android.R

class SuplaLightColors(context: Context) {
  val Primary = colorResource(context, R.color.primary)
  val PrimaryVariant = colorResource(context, R.color.primary_variant)
  val OnPrimary = colorResource(context, R.color.on_primary)
  val Background = colorResource(context, R.color.background)
  val OnBackground = colorResource(context, R.color.on_background)
  val Surface = colorResource(context, R.color.surface)
  val OnSurface = colorResource(context, R.color.on_background)
  val Error = colorResource(context, R.color.red_alert)
  val OnError = Color(0xFFFFFFFF)
  val Scrim = colorResource(context, R.color.dialog_scrim)

  fun toMaterial() = lightColors(
    primary = Primary,
    primaryVariant = PrimaryVariant,
    onPrimary = OnPrimary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    error = Error,
    onError = OnError
  )

  fun toMaterial3() = ColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Surface,
    onPrimaryContainer = OnSurface,
    inversePrimary = Primary,
    secondary = PrimaryVariant,
    onSecondary = OnPrimary,
    secondaryContainer = Surface,
    onSecondaryContainer = OnSurface,
    tertiary = PrimaryVariant,
    onTertiary = OnPrimary,
    tertiaryContainer = Surface,
    onTertiaryContainer = OnSurface,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Surface,
    onSurfaceVariant = OnSurface,
    surfaceTint = OnSurface,
    inverseSurface = Surface,
    inverseOnSurface = OnSurface,
    error = Error,
    onError = OnError,
    errorContainer = Error,
    onErrorContainer = OnError,
    outline = Color(0xFF000000),
    outlineVariant = Color(0xFF000000),
    scrim = Scrim
  )
}

class SuplaDarkColors(context: Context) {
  val Primary = colorResource(context, R.color.primary)
  val PrimaryVariant = colorResource(context, R.color.primary_variant)
  val OnPrimary = colorResource(context, R.color.on_primary)
  val Background = colorResource(context, R.color.background)
  val OnBackground = colorResource(context, R.color.on_background)
  val Surface = colorResource(context, R.color.surface)
  val OnSurface = colorResource(context, R.color.on_background)
  val Error = colorResource(context, R.color.red_alert)
  val OnError = Color(0xFFF5F6F7)
  val Scrim = colorResource(context, R.color.dialog_scrim)

  fun toMaterial() = darkColors(
    primary = Primary,
    primaryVariant = PrimaryVariant,
    onPrimary = OnPrimary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    error = Error,
    onError = OnError
  )

  fun toMaterial3() = ColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Surface,
    onPrimaryContainer = OnSurface,
    inversePrimary = Primary,
    secondary = PrimaryVariant,
    onSecondary = OnPrimary,
    secondaryContainer = Surface,
    onSecondaryContainer = OnSurface,
    tertiary = PrimaryVariant,
    onTertiary = OnPrimary,
    tertiaryContainer = Surface,
    onTertiaryContainer = OnSurface,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Surface,
    onSurfaceVariant = OnSurface,
    surfaceTint = OnSurface,
    inverseSurface = Surface,
    inverseOnSurface = OnSurface,
    error = Error,
    onError = OnError,
    errorContainer = Error,
    onErrorContainer = OnError,
    outline = Color(0xFF000000),
    outlineVariant = Color(0xFF000000),
    scrim = Scrim
  )
}

val Colors.progressPointShadow: Color
  get() = Color(0x99B2F4B8)

val Colors.blue: Color
  get() = Color(0xFF007AFF)

val Colors.gray: Color
  get() = Color(0xFF7E8082)

val ColorScheme.gray: Color
  get() = Color(0xFF7E8082)

val Color.Companion.lightBlue: Color
  get() = Color(0xFF8C9DFF)

val Color.Companion.lightGreen: Color
  get() = Color(0xFFB0E0A8)

val Color.Companion.lightOrange: Color
  get() = Color(0xFFFFD19A)

val Color.Companion.lightRed: Color
  get() = Color(0xFFFFAA8C)

val Color.Companion.disabled: Color
  get() = Color(0xFFB4B7BA)

val Color.Companion.primaryLight: Color
  @Composable
  get() = colorResource(LocalContext.current, R.color.primary_light)

private fun colorResource(context: Context, @ColorRes id: Int): Color {
  return if (Build.VERSION.SDK_INT >= 23) {
    Color(context.resources.getColor(id, context.theme))
  } else {
    @Suppress("DEPRECATION")
    Color(context.resources.getColor(id))
  }
}
