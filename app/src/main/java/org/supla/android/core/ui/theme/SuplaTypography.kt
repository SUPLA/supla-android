package org.supla.android.core.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.supla.android.R

val SuplaTypography = Typography(
  defaultFontFamily = FontFamily(
    Font(R.font.quicksand_light, FontWeight.Light),
    Font(R.font.quicksand_regular, FontWeight.Normal),
    Font(R.font.quicksand_medium, FontWeight.Medium)
  ),
  h1 = TextStyle(fontSize = 96.sp, letterSpacing = 0.15.sp, fontWeight = FontWeight.Light),
  h2 = TextStyle(fontSize = 60.sp, letterSpacing = 0.05.sp, fontWeight = FontWeight.Light),
  h3 = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Normal),
  h4 = TextStyle(fontSize = 34.sp, letterSpacing = 0.025.sp, fontWeight = FontWeight.Normal),
  h5 = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Normal),
  h6 = TextStyle(fontSize = 20.sp, letterSpacing = 0.015.sp, fontWeight = FontWeight.Medium),
  subtitle1 = TextStyle(fontSize = 16.sp, letterSpacing = 0.015.sp, fontWeight = FontWeight.Normal),
  subtitle2 = TextStyle(fontSize = 14.sp, letterSpacing = 0.01.sp, fontWeight = FontWeight.Medium),
  body1 = TextStyle(fontSize = 16.sp, letterSpacing = 0.05.sp, fontWeight = FontWeight.Normal),
  body2 = TextStyle(fontSize = 14.sp, letterSpacing = 0.025.sp, fontWeight = FontWeight.Normal),
  button = TextStyle(fontSize = 14.sp, letterSpacing = 0.125.sp, fontWeight = FontWeight.Medium),
  caption = TextStyle(fontSize = 12.sp, letterSpacing = 0.04.sp, fontWeight = FontWeight.Normal),
  overline = TextStyle(fontSize = 10.sp, letterSpacing = 0.15.sp, fontWeight = FontWeight.Normal),
)