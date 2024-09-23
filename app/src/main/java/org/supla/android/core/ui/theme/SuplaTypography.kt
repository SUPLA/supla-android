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

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import org.supla.android.R
import org.supla.android.extensions.fontDimensionResource

@Composable
fun suplaTypography() = Typography(
  displayLarge = TextStyle(
    fontSize = 60.sp,
    letterSpacing = TextUnit(0.05f, TextUnitType.Em),
    fontWeight = FontWeight.Light,
    fontFamily = OpenSansFontFamily
  ),
  displayMedium = TextStyle(
    fontSize = 48.sp,
    fontWeight = FontWeight.Normal,
    fontFamily = OpenSansFontFamily
  ),
  displaySmall = TextStyle(
    fontSize = 34.sp,
    letterSpacing = TextUnit(0.025f, TextUnitType.Em),
    fontWeight = FontWeight.Normal,
    fontFamily = OpenSansFontFamily
  ),

  headlineLarge = TextStyle(
    fontSize = 34.sp,
    letterSpacing = TextUnit(0.025f, TextUnitType.Em),
    fontWeight = FontWeight.Normal,
    fontFamily = OpenSansFontFamily
  ),
  headlineMedium = TextStyle(
    fontSize = 24.sp,
    fontWeight = FontWeight.Normal,
    fontFamily = OpenSansFontFamily
  ),
  headlineSmall = TextStyle(
    fontSize = 17.sp,
    fontWeight = FontWeight.Normal,
    fontFamily = OpenSansFontFamily
  ),

  titleLarge = TextStyle(
    fontSize = 22.sp,
    letterSpacing = TextUnit(0.025f, TextUnitType.Em),
    fontWeight = FontWeight.Normal,
    fontFamily = OpenSansFontFamily
  ),
  titleMedium = TextStyle(
    fontSize = 16.sp,
    fontWeight = FontWeight.SemiBold,
    fontFamily = OpenSansFontFamily
  ),
  titleSmall = TextStyle(
    fontSize = 14.sp,
    letterSpacing = TextUnit(0.015f, TextUnitType.Em),
    fontWeight = FontWeight.SemiBold,
    fontFamily = OpenSansFontFamily
  ),

  bodyLarge = TextStyle(
    fontSize = 16.sp,
    letterSpacing = TextUnit(0.005f, TextUnitType.Em),
    fontWeight = FontWeight.Normal,
    fontFamily = OpenSansFontFamily
  ),
  bodyMedium = TextStyle(
    fontSize = 14.sp,
    letterSpacing = TextUnit(0.025f, TextUnitType.Em),
    fontWeight = FontWeight.Normal,
    fontFamily = OpenSansFontFamily
  ),
  bodySmall = TextStyle(
    fontSize = 12.sp,
    letterSpacing = TextUnit(0.025f, TextUnitType.Em),
    fontWeight = FontWeight.Normal,
    fontFamily = OpenSansFontFamily
  ),

  labelLarge = TextStyle(
    fontSize = 17.sp,
    letterSpacing = TextUnit(0.015f, TextUnitType.Em),
    fontWeight = FontWeight.Medium,
    fontFamily = OpenSansFontFamily
  ),
  labelMedium = TextStyle(
    fontSize = 14.sp,
    letterSpacing = TextUnit(0.015f, TextUnitType.Em),
    fontWeight = FontWeight.Bold,
    fontFamily = OpenSansFontFamily
  ),
  labelSmall = TextStyle(
    fontSize = 10.sp,
    letterSpacing = TextUnit(0.015f, TextUnitType.Em),
    fontWeight = FontWeight.SemiBold,
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

@Composable
fun Typography.listItemValue(): TextStyle =
  TextStyle(
    color = androidx.compose.ui.res.colorResource(id = R.color.on_background),
    fontSize = fontDimensionResource(id = R.dimen.channel_imgtext_size),
    fontWeight = FontWeight.Normal,
    fontFamily = FontFamily(Font(R.font.open_sans_regular))
  )
