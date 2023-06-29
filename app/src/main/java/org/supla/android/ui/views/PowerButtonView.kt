package org.supla.android.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.SuplaTypography

// special colors
private val borderColorNormal = Color(0xFFB4B7BA)
private val backgroundGradient1 = Color(0xFFFFFFFF)
private val backgroundGradient2 = Color(0xFFF4F4F4)
private val negativeColor = Color(0xFFEB3A28)

class PowerButtonView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

  var icon: Bitmap? by mutableStateOf(null)
  var text: String? by mutableStateOf(null)
  var type: Type by mutableStateOf(Type.POSITIVE)
  var clickListener: () -> Unit = {  }

  init {
    context.theme.obtainStyledAttributes(attrs, R.styleable.PowerButtonView, 0, 0).apply {
      try {
        text = getString(R.styleable.PowerButtonView_text)
        type = getInteger(R.styleable.PowerButtonView_type, 0).toType()
      } finally {
        recycle()
      }
    }
  }

  @Composable
  override fun Content() {
    SuplaTheme {
      PowerButtonView(icon, text, type, clickListener)
    }
  }

  enum class Type(val value: Int) {
    POSITIVE(0), NEGATIVE(1)
  }

  private fun Int.toType(): Type {
    for (type in Type.values()) {
      if (type.value == this) {
        return type
      }
    }

    throw IllegalStateException("No type for `$this`")
  }
}

@Composable
fun PowerButtonView(
  icon: Bitmap? = null,
  text: String? = null,
  type: PowerButtonView.Type = PowerButtonView.Type.POSITIVE,
  onClick: () -> Unit,
  size: Dp = 140.dp,
  padding: Dp = 10.dp
) {

  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val pressedColor = when(type) {
    PowerButtonView.Type.POSITIVE -> MaterialTheme.colors.primary
    PowerButtonView.Type.NEGATIVE -> negativeColor
  }
  val borderColor = if (isPressed) pressedColor else borderColorNormal
  val shadowColor = if (isPressed) pressedColor else DefaultShadowColor
  val textColor = when (type) {
    PowerButtonView.Type.POSITIVE -> if (isPressed) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.onSurface
    PowerButtonView.Type.NEGATIVE -> if (isPressed) negativeColor else MaterialTheme.colors.onSurface
  }

  Box(
    modifier = Modifier
      .width(size)
      .height(size)
      .padding(all = padding)
      .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(size = size.minus(padding.times(2))))
      .shadow(elevation = 4.dp, shape = CircleShape, ambientColor = shadowColor, spotColor = shadowColor)
      .background(brush = Brush.radialGradient(listOf(backgroundGradient1, backgroundGradient2)))
      .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      icon?.let {
        Image(
          bitmap = icon.asImageBitmap(),
          contentDescription = "",
          alignment = Alignment.Center,
          modifier = Modifier.size(24.dp)
        )
      }
      text?.let {
        Text(text = text, style = SuplaTypography.button, color = textColor)
      }
    }
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(modifier = Modifier.background(color = Color(0xFFF5F6F7))) {
      PowerButtonView(onClick = {})
      PowerButtonView(text = "Turn on", onClick = {})
    }
  }
}
