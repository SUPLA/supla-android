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
  MaterialTheme(
    colors = if (darkMode) SuplaDarkColors(LocalContext.current).toMaterial() else SuplaLightColors(LocalContext.current).toMaterial(),
    typography = SuplaTypography,
    content = content
  )
}
