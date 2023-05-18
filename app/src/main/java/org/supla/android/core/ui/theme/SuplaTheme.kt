package org.supla.android.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun SuplaTheme(
  darkMode: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colors = if (darkMode) SuplaDarkColors.toMaterial() else SuplaLightColors.toMaterial(),
    typography = SuplaTypography,
    content = content
  )
}