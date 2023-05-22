package org.supla.android.core.ui.theme

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

object SuplaLightColors {
  val Primary = Color(0xFF12A71E)
  val PrimaryVariant = Color(0xFF00D151)
  val OnPrimary = Color(0xFFFFFFFF)
  val Background = Color(0xFFF5F6F7)
  val OnBackground = Color(0xFF000000)
  val Surface = Color(0xFFFFFFFF)
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

object SuplaDarkColors {
  val Primary = Color(0xFF2AA75A)
  val PrimaryVariant = Color(0xFF2AA75A)
  val OnPrimary = Color(0xFFF5F6F7)
  val Background = Color(0xFF262627)
  val OnBackground = Color(0xFFB4B7BA)
  val Surface = Color(0xFF171717)
  val OnSurface = Color(0xFFF5F6F7)
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
