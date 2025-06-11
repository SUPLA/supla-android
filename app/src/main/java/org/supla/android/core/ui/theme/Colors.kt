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
import androidx.annotation.ColorRes
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

object SuplaColors {
  val light = ColorScheme(
    primary = Color(0xFF12A71E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF12A71E),
    onPrimaryContainer = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFF12A71E),
    secondary = Color(0xFF007AFF),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE7F2FF),
    onSecondaryContainer = Color(0xFF282828),
    tertiary = Color(0xFFAD6309),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFFFFF),
    onTertiaryContainer = Color(0xFF282828),
    background = Color(0xFFF5F6F7),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF282828),
    surfaceVariant = Color(0xFFEDFBEE),
    onSurfaceVariant = Color(0xFF7E8082),
    surfaceTint = Color(0xFF282828),
    inverseSurface = Color(0xFF282828),
    inverseOnSurface = Color(0xFFFFFFFF),
    error = Color(0xFFEB3A28),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFAEAE9),
    onErrorContainer = Color(0xFFFFFFFF),
    outline = Color(0xFFE1E2E3),
    outlineVariant = Color(0x14282828),
    scrim = Color(0x80F5F6F7),
    surfaceBright = Color.Unspecified,
    surfaceDim = Color.Unspecified,
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF)
  )

  val dark = ColorScheme(
    primary = Color(0xFF12A71E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF003910),
    onPrimaryContainer = Color(0xFFE4E4E4),
    inversePrimary = Color(0xFF12A71E),
    secondary = Color(0xFF54A6FF),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF03162C),
    onSecondaryContainer = Color(0xFFE4E4E4),
    tertiary = Color(0xFFC8965A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF262626),
    onTertiaryContainer = Color(0xFFE4E4E4),
    background = Color(0xFF131313),
    onBackground = Color(0xFFFCFCFC),
    surface = Color(0xFF262626),
    onSurface = Color(0xFFE4E4E4),
    surfaceVariant = Color(0xFF191B1A),
    onSurfaceVariant = Color(0xFFB2B2B2),
    surfaceTint = Color(0xFFE4E4E4),
    inverseSurface = Color(0xFFE4E4E4),
    inverseOnSurface = Color(0xFF262626),
    error = Color(0xFFEF6153),
    onError = Color(0xFFF5F6F7),
    errorContainer = Color(0xFF270603),
    onErrorContainer = Color(0xFFF5F6F7),
    outline = Color(0xFF565656),
    outlineVariant = Color(0xFF363636),
    scrim = Color(0xBB171717),
    surfaceBright = Color.Unspecified,
    surfaceDim = Color.Unspecified,
    surfaceContainer = Color(0xFF262626),
    surfaceContainerHigh = Color(0xFF262626),
    surfaceContainerHighest = Color(0xFF262626),
    surfaceContainerLow = Color(0xFF262626),
    surfaceContainerLowest = Color(0xFF262626),
  )
}

val ColorScheme.progressPointShadow: Color
  get() = Color(0x99B2F4B8)

val ColorScheme.gray: Color
  get() = Color(0xFF7E8082)

private fun colorResource(context: Context, @ColorRes id: Int): Color {
  return Color(context.resources.getColor(id, context.theme))
}
