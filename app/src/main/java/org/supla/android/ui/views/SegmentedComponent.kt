package org.supla.android.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme

class SegmentedComponent @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

  var selectedItemListener: (Int) -> Unit = { }

  var items by mutableStateOf(listOf<String>())
  var activeItem by mutableStateOf(0)
  var disabled by mutableStateOf(false)

  @Composable
  override fun Content() {
    SuplaTheme {
      SegmentedComponent(items, activeItem = activeItem, disabled = disabled) {
        if (disabled.not()) {
          activeItem = it
          selectedItemListener(it)
        }
      }
    }
  }
}

@Composable
fun SegmentedComponent(
  items: List<String>,
  modifier: Modifier = Modifier,
  activeItem: Int = 0,
  disabled: Boolean = false,
  onClick: (Int) -> Unit = {}
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .background(color = colorResource(id = R.color.segmented_field_background), shape = RoundedCornerShape(6.dp))
      .padding(2.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    for ((i, item) in items.withIndex()) {
      val textModifier = if (activeItem == i) {
        Modifier.background(colorResource(id = R.color.field_background), shape = RoundedCornerShape(6.dp))
      } else {
        Modifier
      }

      ClickableText(
        text = AnnotatedString(item),
        style = MaterialTheme.typography.body2.copy(
          color = if (disabled) colorResource(id = R.color.item_unselected) else MaterialTheme.colors.onBackground,
          textAlign = TextAlign.Center
        ),
        onClick = {
          if (activeItem != i) {
            onClick(i)
          }
        },
        modifier = textModifier
          .padding(horizontal = 8.dp, vertical = 8.dp)
          .weight(1f)
      )
    }
  }
}

@Preview
@Composable
private fun Preview() {
  Box(modifier = Modifier.background(Color.White)) {
    SuplaTheme {
      Column {
        SegmentedComponent(listOf("Turn on", "Turn off"), activeItem = 1, disabled = true)
        SegmentedComponent(listOf("Turn on", "Turn off"), activeItem = 0, disabled = false)
      }
    }
  }
}
