package org.supla.android.core.ui.theme

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

val SuplaTypography = Typography(
  defaultFontFamily = FontFamily(
    Font(R.font.open_sans_light, FontWeight.Light),
    Font(R.font.open_sans_regular, FontWeight.Normal),
    Font(R.font.open_sans_medium, FontWeight.Medium),
    Font(R.font.open_sans_semibold, FontWeight.SemiBold),
    Font(R.font.open_sans_bold, FontWeight.Bold)
  ),
  h1 = TextStyle(fontSize = 96.sp, letterSpacing = 0.15.sp, fontWeight = FontWeight.Light),
  h2 = TextStyle(fontSize = 60.sp, letterSpacing = 0.05.sp, fontWeight = FontWeight.Light),
  h3 = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Normal),
  h4 = TextStyle(fontSize = 34.sp, letterSpacing = 0.025.sp, fontWeight = FontWeight.Normal),
  h5 = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Normal),
  h6 = TextStyle(fontSize = 17.sp, letterSpacing = 0.015.sp, fontWeight = FontWeight.SemiBold),
  subtitle1 = TextStyle(fontSize = 16.sp, letterSpacing = 0.015.sp, fontWeight = FontWeight.Normal),
  subtitle2 = TextStyle(fontSize = 14.sp, letterSpacing = 0.01.sp, fontWeight = FontWeight.Medium),
  body1 = TextStyle(fontSize = 16.sp, letterSpacing = 0.05.sp, fontWeight = FontWeight.Normal),
  body2 = TextStyle(fontSize = 14.sp, letterSpacing = 0.025.sp, fontWeight = FontWeight.Normal),
  button = TextStyle(fontSize = 17.sp, letterSpacing = 0.125.sp, fontWeight = FontWeight.Medium),
  caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
  overline = TextStyle(fontSize = 10.sp, letterSpacing = 0.15.sp, fontWeight = FontWeight.Normal)
)

@Composable
fun Typography.listItemCaption(): TextStyle =
  TextStyle(
    color = colorResource(id = R.color.channel_caption_text),
    fontSize = fontDimensionResource(id = R.dimen.channel_caption_text_size),
    letterSpacing = 0.04.sp,
    fontWeight = FontWeight.Bold,
    fontFamily = FontFamily(Font(R.font.open_sans_bold))
  )

@Composable
fun Typography.listItemValue(): TextStyle =
  TextStyle(
    color = colorResource(id = R.color.channel_imgtext_color),
    fontSize = fontDimensionResource(id = R.dimen.channel_imgtext_size),
    fontWeight = FontWeight.Normal,
    fontFamily = FontFamily(Font(R.font.open_sans_regular))
  )
