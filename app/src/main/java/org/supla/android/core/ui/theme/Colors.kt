package org.supla.android.core.ui.theme

import android.content.Context
import android.os.Build
import androidx.annotation.ColorRes
import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
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
}

val Colors.progressPointShadow: Color
  get() = Color(0x99B2F4B8)

val Colors.blue: Color
  get() = Color(0xFF007AFF)

val Colors.grey: Color
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
