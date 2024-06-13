package org.supla.android.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun SuplaTheme(
  darkMode: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val suplaDarkColors = SuplaDarkColors(LocalContext.current)
  val suplaLightColors = SuplaLightColors(LocalContext.current)
  val colors = if (darkMode) suplaDarkColors.toMaterial() else suplaLightColors.toMaterial()
  val colorScheme = if (darkMode) suplaDarkColors.toMaterial3() else suplaLightColors.toMaterial3()

  MaterialTheme(
    colors = colors,
    typography = SuplaTypographyMaterial2(colors),
    content = {
      androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        content = content,
        typography = SuplaTypographyMaterial3(colors)
      )
    }
  )
}
