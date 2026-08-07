package androidx.compose.material3
/* Copied from androidx.cmopose.material3.NavigationBar and adopted for smaller height */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance

@Composable
fun ResizeableNavigationBar(
  minHeight: Dp = dimensionResource(R.dimen.bottom_bar_height),
  content: @Composable RowScope.() -> Unit,
) {
  val outlineColor = MaterialTheme.colorScheme.outline

  Surface(
    color = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
    tonalElevation = NavigationBarDefaults.Elevation,
    modifier = Modifier
      .drawWithContent {
        drawContent()
        val strokeWidth = 1.dp.toPx()
        drawLine(
          color = outlineColor,
          start = Offset(0f, strokeWidth / 2),
          end = Offset(size.width, strokeWidth / 2),
          strokeWidth = strokeWidth
        )
      },
  ) {
    Row(
      modifier =
      Modifier
        .fillMaxWidth()
        .windowInsetsPadding(NavigationBarDefaults.windowInsets)
        .defaultMinSize(minHeight = minHeight)
        .selectableGroup(),
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
      verticalAlignment = Alignment.CenterVertically,
      content = content,
    )
  }
}
