package org.supla.android.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

  @Composable
  override fun Content() {
    SuplaTheme {
      SegmentedComponentContent(items, activeItem) {
        activeItem = it
        selectedItemListener(it)
      }
    }
  }
}

@Composable
private fun SegmentedComponentContent(items: List<String>, activeItem: Int, onClick: (Int) -> Unit = {}) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .background(color = colorResource(id = R.color.switcher_background_light), shape = RoundedCornerShape(6.dp))
      .padding(2.dp)
  ) {
    for ((i, item) in items.withIndex()) {
      if (i > 0) {
        Spacer(modifier = Modifier.width(10.dp))
      }
      if (activeItem == i) {
        ClickableText(
          text = AnnotatedString(item.uppercase()),
          onClick = { },
          modifier = Modifier
            .background(MaterialTheme.colors.onPrimary, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        )
      } else {
        ClickableText(
          text = AnnotatedString(item.uppercase()),
          onClick = { onClick(i) },
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }
  }
}

@Preview
@Composable
private fun Preview() {
  Box(modifier = Modifier.background(Color.White)) {
    SuplaTheme {
      SegmentedComponentContent(listOf("Turn on", "Turn off"), 1)
    }
  }
}