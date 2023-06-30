package org.supla.android.core.ui.theme

import android.content.Context
import android.os.Build
import androidx.annotation.ColorRes
import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color
import org.supla.android.R

class SuplaLightColors(context: Context) {
  val Primary = colorResource(context, R.color.primary)
  val PrimaryVariant = colorResource(context, R.color.primary_variant)
  val OnPrimary = colorResource(context, R.color.on_primary)
  val Background = colorResource(context, R.color.background)
  val OnBackground = Color(0xFF000000)
  val Surface = colorResource(context, R.color.surface)
  val OnSurface = Color(0xFF000000)
  val Error = Color(0xFFEB3A28)
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
  val OnBackground = Color(0xFF000000)
  val Surface = colorResource(context, R.color.surface)
  val OnSurface = Color(0xFF000000)
  val Error = Color(0xFFEB3A28)
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

private fun colorResource(context: Context, @ColorRes id: Int): Color {
  return if (Build.VERSION.SDK_INT >= 23) {
    Color(context.resources.getColor(id, context.theme))
  } else {
    @Suppress("DEPRECATION")
    Color(context.resources.getColor(id))
  }
}
