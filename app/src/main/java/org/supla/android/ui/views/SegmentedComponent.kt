package org.supla.android.ui.views
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.colorResource
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
  var activeItem by mutableIntStateOf(0)
  var disabled by mutableStateOf(false)

  @Composable
  override fun Content() {
    SuplaTheme {
      SegmentedComponent(items, activeItem = activeItem, enabled = !disabled) {
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
  enabled: Boolean = true,
  onClick: (Int) -> Unit = {}
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .background(color = colorResource(id = R.color.segmented_field_background), shape = RoundedCornerShape(6.dp))
      .height(IntrinsicSize.Max)
      .padding(2.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    for ((i, item) in items.withIndex()) {
      val textModifier = if (activeItem == i) {
        Modifier.background(colorResource(id = R.color.field_background), shape = RoundedCornerShape(6.dp))
      } else {
        Modifier
      }

      Text(
        text = item,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = if (enabled) MaterialTheme.colorScheme.onBackground else colorResource(id = R.color.item_unselected),
          textAlign = TextAlign.Center
        ),
        modifier = textModifier
          .padding(horizontal = 8.dp, vertical = 8.dp)
          .fillMaxHeight()
          .weight(1f)
          .clickable {
            if (activeItem != i && enabled) {
              onClick(i)
            }
          }
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
        SegmentedComponent(listOf("Turn on", "Turn off"), activeItem = 1, enabled = true)
        SegmentedComponent(listOf("Turn on", "Turn off"), activeItem = 0, enabled = false)
      }
    }
  }
}
