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

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.supla.android.R
import org.supla.android.extensions.fontDimensionResource

@Composable
fun suplaTypographyMaterial3(colors: ColorScheme) = Typography(
  displayLarge = TextStyle(
    fontSize = 60.sp,
    letterSpacing = 0.05.em,
    fontWeight = FontWeight.Light,
    color = colors.onBackground,
    fontFamily = OpenSansFontFamily
  ),
  displayMedium = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Normal, color = colors.onBackground, fontFamily = OpenSansFontFamily),
  displaySmall = TextStyle(
    fontSize = 34.sp,
    letterSpacing = 0.025.sp,
    fontWeight = FontWeight.Normal,
    color = colors.onBackground,
    fontFamily = OpenSansFontFamily
  ),

  headlineLarge = TextStyle(
    fontSize = 34.sp,
    letterSpacing = 0.025.sp,
    fontWeight = FontWeight.Normal,
    color = colors.onBackground,
    fontFamily = OpenSansFontFamily
  ),
  headlineMedium = TextStyle(
    fontSize = 24.sp,
    fontWeight = FontWeight.Normal,
    color = colors.onBackground,
    fontFamily = OpenSansFontFamily
  ),
  headlineSmall = TextStyle(
    fontSize = 17.sp,
    fontWeight = FontWeight.Normal,
    color = colors.onBackground,
    fontFamily = OpenSansFontFamily
  ),

  titleLarge = TextStyle(
    fontSize = 22.sp,
    letterSpacing = 0.025.sp,
    fontWeight = FontWeight.Normal,
    color = colors.onBackground,
    fontFamily = OpenSansFontFamily
  ),
  titleMedium = TextStyle(
    fontSize = 16.sp,
    fontWeight = FontWeight.SemiBold,
    color = colors.onBackground,
    fontFamily = OpenSansFontFamily
  ),
  titleSmall = TextStyle(
    fontSize = 14.sp,
    letterSpacing = 0.015.sp,
    fontWeight = FontWeight.SemiBold,
    color = colors.onBackground,
    fontFamily = OpenSansFontFamily
  ),

  bodyLarge = TextStyle(
    fontSize = 16.sp,
    letterSpacing = 0.05.sp,
    fontWeight = FontWeight.Normal,
    color = colors.onBackground,
    fontFamily = OpenSansFontFamily
  ),
  bodyMedium = TextStyle(
    fontSize = 14.sp,
    letterSpacing = 0.025.sp,
    fontWeight = FontWeight.Normal,
    color = colors.onBackground,
    fontFamily = OpenSansFontFamily
  ),
  bodySmall = TextStyle(
    fontSize = 12.sp,
    letterSpacing = 0.025.sp,
    fontWeight = FontWeight.Normal,
    color = colors.onBackground,
    fontFamily = OpenSansFontFamily
  ),

  labelLarge = TextStyle(
    fontSize = 17.sp,
    letterSpacing = 0.15.sp,
    fontWeight = FontWeight.Medium,
    color = colors.onBackground,
    fontFamily = OpenSansFontFamily
  ),
  labelMedium = TextStyle(
    fontSize = 12.sp,
    letterSpacing = 0.15.sp,
    fontWeight = FontWeight.SemiBold,
    color = colors.onBackground,
    fontFamily = OpenSansFontFamily
  ),
  labelSmall = TextStyle(
    fontSize = 10.sp,
    letterSpacing = 0.15.sp,
    fontWeight = FontWeight.SemiBold,
    color = colors.onBackground,
    fontFamily = OpenSansFontFamily
  )
)

@Composable
fun Typography.listItemCaption(): TextStyle =
  TextStyle(
    color = androidx.compose.ui.res.colorResource(id = R.color.on_background),
    fontSize = fontDimensionResource(id = R.dimen.channel_caption_text_size),
    letterSpacing = 0.04.sp,
    fontWeight = FontWeight.Bold,
    fontFamily = FontFamily(Font(R.font.open_sans_bold))
  )
