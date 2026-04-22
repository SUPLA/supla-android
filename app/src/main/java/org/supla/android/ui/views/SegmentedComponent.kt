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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.switchdetail.timer.TimerTargetAction
import org.supla.android.tools.SuplaPreview
import org.supla.core.shared.infrastructure.LocalizedString

interface SegmentedComponentItem {
  val label: LocalizedString
}

enum class BoxSize {
  Custom, Identical
}

class SegmentedComponent @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

  var selectedItemListener: (TimerTargetAction) -> Unit = { }

  var items by mutableStateOf(listOf<TimerTargetAction>())
  var activeItem by mutableStateOf<TimerTargetAction?>(null)
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
fun <T : SegmentedComponentItem> SegmentedComponent(
  items: List<T>,
  modifier: Modifier = Modifier,
  activeItem: T? = null,
  enabled: Boolean = true,
  boxSize: BoxSize = BoxSize.Identical,
  onClick: (T) -> Unit = {}
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .background(color = colorResource(id = R.color.segmented_field_background), shape = RoundedCornerShape(6.dp))
      .height(IntrinsicSize.Max)
      .padding(2.dp),
    horizontalArrangement = if (boxSize == BoxSize.Custom) Arrangement.SpaceEvenly else Arrangement.Start
  ) {
    items.forEachIndexed { index, item ->
      val textModifier = if (activeItem == item) {
        Modifier
          .background(colorResource(id = R.color.field_background), shape = RoundedCornerShape(6.dp))
      } else {
        Modifier
      }

      Text(
        text = item.label(),
        style = MaterialTheme.typography.bodyMedium.copy(
          color = if (enabled) MaterialTheme.colorScheme.onBackground else colorResource(id = R.color.item_unselected),
          textAlign = TextAlign.Center
        ),
        modifier = textModifier
          .clickable(enabled = activeItem != item && enabled) { onClick(item) }
          .padding(horizontal = 8.dp, vertical = 8.dp)
          .let { if (boxSize == BoxSize.Identical) it.weight(1f) else it }
          .fillMaxHeight()
      )

      if (index < items.size - 1) {
        Spacer(modifier = Modifier.width(Distance.tiny))
      }
    }
  }
}

@SuplaPreview
@Composable
private fun Preview() {
  Box(modifier = Modifier.background(Color.White)) {
    SuplaTheme {
      Column {
        SegmentedComponent(TimerTargetAction.entries, activeItem = TimerTargetAction.TURN_OFF, enabled = true)
        SegmentedComponent(TimerTargetAction.entries, activeItem = TimerTargetAction.TURN_ON, enabled = false)
        SegmentedComponent(
          TimerTargetAction.entries,
          activeItem = TimerTargetAction.TURN_ON,
          boxSize = BoxSize.Custom,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}
