package org.supla.android.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.SuplaTypography
import org.supla.android.extensions.innerShadow

// special colors
private val borderColorNormal = Color(0xFFB4B7BA)
private val negativeColor = Color(0xFFEB3A28)
private val transparentColor = Color(0x00FFFFFF)
private val innerShadowColor = Color(0xFFB4B7BA)

class PowerButtonView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

  var icon: Bitmap? by mutableStateOf(null)
  var text: String? by mutableStateOf(null)
  var type: Type by mutableStateOf(Type.POSITIVE)
  var clickListener: () -> Unit = { }

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

  val pressedColor = when (type) {
    PowerButtonView.Type.POSITIVE -> MaterialTheme.colors.primary
    PowerButtonView.Type.NEGATIVE -> negativeColor
  }
  val borderColor = remember { AnimatableColor(pressedColor, borderColorNormal) }
  val outerShadowColor = remember { AnimatableColor(pressedColor, DefaultShadowColor) }
  val innerShadowColor = remember { AnimatableColor(innerShadowColor, transparentColor) }
  val primaryVariantColor = MaterialTheme.colors.primaryVariant
  val onSurfaceColor = MaterialTheme.colors.onSurface
  val textColor = when (type) {
    PowerButtonView.Type.POSITIVE -> remember { AnimatableColor(primaryVariantColor, onSurfaceColor) }
    PowerButtonView.Type.NEGATIVE -> remember { AnimatableColor(negativeColor, onSurfaceColor) }
  }

  LaunchedEffect(interactionSource) {
    launch {
      interactionSource.interactions.collect { interaction ->
        when (interaction) {
          is PressInteraction.Press -> {
            awaitAll(
              async { borderColor.animate(true) },
              async { outerShadowColor.animate(true) },
              async { innerShadowColor.animate(true) },
              async { textColor.animate(true) }
            )
          }
          is PressInteraction.Release,
          is PressInteraction.Cancel -> {
            awaitAll(
              async { borderColor.animate(false) },
              async { outerShadowColor.animate(false) },
              async { innerShadowColor.animate(false) },
              async { textColor.animate(false) }
            )
          }
        }
      }
    }
  }

  Box(
    modifier = Modifier
      .width(size)
      .height(size)
      .padding(all = padding)
      .border(width = 1.dp, color = borderColor.color, shape = RoundedCornerShape(size = size.minus(padding.times(2))))
      .shadow(elevation = 4.dp, shape = CircleShape, ambientColor = outerShadowColor.color, spotColor = outerShadowColor.color)
      .innerShadow(color = innerShadowColor.color, blur = 5.dp, cornersRadius = size.minus(padding.times(2)), offsetY = 4.dp)
      .background(color = MaterialTheme.colors.surface)
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
        Text(text = text, style = SuplaTypography.button, color = textColor.color)
      }
    }
  }
}

private class AnimatableColor(val destinationColor: Color, val initialColor: Color) {
  private val animatable = Animatable(initialColor)
  val color: Color
    get() = animatable.value

  suspend fun animate(isPressed: Boolean) {
    if (isPressed) {
      animatable.snapTo(initialColor)
      animatable.animateTo(destinationColor, animationSpec = SpringSpec(stiffness = Spring.StiffnessHigh))
    } else {
      animatable.animateTo(initialColor, animationSpec = SpringSpec(stiffness = Spring.StiffnessHigh.times(2)))
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
