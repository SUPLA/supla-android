package org.supla.android.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.SuplaTypography

class DetailStartButton @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

  var buttonText by mutableStateOf("")
  var running by mutableStateOf(false)

  @Composable
  override fun Content() {
    SuplaTheme {
      DetailStartButtonContent(buttonText, running)
    }
  }
}

@Composable
internal fun DetailStartButtonContent(text: String, running: Boolean) {
  Box(contentAlignment = Alignment.Center) {
    val color = if (running) { colorResource(id = R.color.red_alert) } else { MaterialTheme.colors.primary }
    Canvas(modifier = Modifier.size(128.dp)) {
      drawCircle(
        color = color,
        radius = 64.dp.toPx(),
        style = Stroke(width = 2.dp.toPx())
      )
      drawCircle(color.copy(alpha = 0.1f), 56.dp.toPx())
    }
    Text(text = text, style = SuplaTypography.button)
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    DetailStartButtonContent("Start", false)
  }
}
