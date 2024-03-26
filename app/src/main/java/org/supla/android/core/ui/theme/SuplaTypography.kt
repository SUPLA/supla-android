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

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.supla.android.R
import org.supla.android.extensions.fontDimensionResource

@Composable
fun SuplaTypography(colors: Colors) = Typography(
  defaultFontFamily = FontFamily(
    Font(R.font.open_sans_light, FontWeight.Light),
    Font(R.font.open_sans_regular, FontWeight.Normal),
    Font(R.font.open_sans_medium, FontWeight.Medium),
    Font(R.font.open_sans_semibold, FontWeight.SemiBold),
    Font(R.font.open_sans_bold, FontWeight.Bold)
  ),
  h1 = TextStyle(fontSize = 96.sp, letterSpacing = 0.15.sp, fontWeight = FontWeight.Light, color = colors.onBackground),
  h2 = TextStyle(fontSize = 60.sp, letterSpacing = 0.05.sp, fontWeight = FontWeight.Light, color = colors.onBackground),
  h3 = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Normal, color = colors.onBackground),
  h4 = TextStyle(fontSize = 34.sp, letterSpacing = 0.025.sp, fontWeight = FontWeight.Normal, color = colors.onBackground),
  h5 = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Normal, color = colors.onBackground),
  h6 = TextStyle(fontSize = 17.sp, letterSpacing = 0.015.sp, fontWeight = FontWeight.SemiBold, color = colors.onBackground),
  subtitle1 = TextStyle(fontSize = 16.sp, letterSpacing = 0.015.sp, fontWeight = FontWeight.Normal, color = colors.onBackground),
  subtitle2 = TextStyle(fontSize = 14.sp, letterSpacing = 0.01.sp, fontWeight = FontWeight.Medium, color = colors.onBackground),
  body1 = TextStyle(fontSize = 16.sp, letterSpacing = 0.05.sp, fontWeight = FontWeight.Normal, color = colors.onBackground),
  body2 = TextStyle(fontSize = 14.sp, letterSpacing = 0.025.sp, fontWeight = FontWeight.Normal, color = colors.onBackground),
  button = TextStyle(fontSize = 17.sp, letterSpacing = 0.125.sp, fontWeight = FontWeight.Medium, color = colors.onBackground),
  caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, color = colors.onBackground),
  overline = TextStyle(fontSize = 10.sp, letterSpacing = 0.15.sp, fontWeight = FontWeight.Normal, color = colors.onBackground)
)

@Composable
fun Typography.listItemCaption(): TextStyle =
  TextStyle(
    color = colorResource(id = R.color.on_background),
    fontSize = fontDimensionResource(id = R.dimen.channel_caption_text_size),
    letterSpacing = 0.04.sp,
    fontWeight = FontWeight.Bold,
    fontFamily = FontFamily(Font(R.font.open_sans_bold))
  )

@Composable
fun Typography.listItemValue(): TextStyle =
  TextStyle(
    color = colorResource(id = R.color.on_background),
    fontSize = fontDimensionResource(id = R.dimen.channel_imgtext_size),
    fontWeight = FontWeight.Normal,
    fontFamily = FontFamily(Font(R.font.open_sans_regular))
  )

@Composable
fun Typography.calendarCaption(): TextStyle =
  TextStyle(
    color = MaterialTheme.colors.onBackground,
    fontSize = 12.sp,
    fontWeight = FontWeight.SemiBold,
    fontFamily = FontFamily(Font(R.font.open_sans_semibold))
  )
