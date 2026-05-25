package org.supla.android.features.locationreorder
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.tools.BACKGROUND_COLOR
import org.supla.android.ui.views.Image
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun LocationReorderScope.View(viewState: LocationReorderViewState) {
  val view = LocalView.current

  Box(modifier = Modifier.fillMaxSize()) {
    if (viewState.locations.isNotEmpty()) {
      val lazyListState = rememberLazyListState()
      val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onMove(from.index, to.index)
        ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
      }

      LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(vertical = Distance.default),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(
          items = viewState.locations,
          key = { it.id!! }
        ) { location ->
          ReorderableItem(reorderableLazyListState, key = location.id!!) {
            LocationRow(
              location = location,
              scope = this
            )
          }
        }
      }
    }
  }
}

@Composable
private fun LocationReorderScope.LocationRow(
  location: LocationEntity,
  scope: ReorderableCollectionItemScope
) =
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .padding(start = Distance.default, end = Distance.small, top = Distance.tiny, bottom = Distance.tiny)
  ) {
    Text(
      text = location.caption.orEmpty(),
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.weight(1f)
    )

    Image(
      drawableId = R.drawable.move_holder,
      modifier = with(scope) {
        Modifier.draggableHandle(onDragStopped = { onMoveFinished() })
      }
    )
  }

private val previewScope = object : LocationReorderScope {
  override fun onMove(from: Int, to: Int) {}
  override fun onMoveFinished() {}
}

@Preview(showBackground = true, backgroundColor = BACKGROUND_COLOR)
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      LocationReorderViewState(
        locations = listOf(
          mockLocation(1L, "Living room"),
          mockLocation(2L, "Kitchen"),
          mockLocation(3L, "Garage")
        )
      )
    )
  }
}

private fun mockLocation(id: Long, caption: String): LocationEntity =
  LocationEntity(
    id = id,
    remoteId = id.toInt(),
    caption = caption,
    visible = 1,
    collapsed = 0,
    sorting = org.supla.android.db.Location.SortingType.DEFAULT,
    sortOrder = 0,
    profileId = 1L
  )
