package org.supla.android.ui.views.list
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.distinctUntilChanged
import org.supla.android.R
import org.supla.android.main.scaffold.screenUnderTopBarPaddings
import org.supla.android.main.scaffold.topSearchBarPaddings
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.lists.LocalSlideableController
import org.supla.android.ui.lists.SlideableListEvent
import org.supla.android.ui.lists.SlideableListItem
import org.supla.android.ui.lists.message
import org.supla.android.ui.views.list.listitem.DoubleIconValueListItemView
import org.supla.android.ui.views.list.listitem.HeatpolThermostatListItemView
import org.supla.android.ui.views.list.listitem.IconValueListItemView
import org.supla.android.ui.views.list.listitem.LocationListItemView
import org.supla.android.ui.views.list.listitem.SceneListItemView
import org.supla.android.ui.views.list.listitem.ThermostatListItemView
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.extensions.forTrue
import org.supla.core.shared.infrastructure.localizedString
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

interface MainListScope {
  fun onLeftButtonClick(remoteId: Int)
  fun onRightButtonClick(remoteId: Int)
  fun swapItems(from: Int, to: Int): Boolean
  fun onDragStopped(remoteId: Int)
  fun onLocationClick(remoteId: Int)
  fun onItemClick(remoteId: Int)
  fun onTitleLongClick(item: ListItem)
  fun onLocationLongClick(item: ListItem.LocationItem)

  // Optional
  fun onInfoClick(remoteId: Int) {}
  fun onIssueClick(message: String) {}
}

@Composable
fun MainListScope.ListView(
  items: List<ListItem>,
  modifier: Modifier = Modifier
) {
  val offsets = remember { mutableStateMapOf<Int, Float>() }
  val lazyListState = rememberLazyListState()
  val reorderableLazyListState = rememberReorderableLazyListState(
    lazyListState = lazyListState
  ) { from, to ->
    swapItems(from.index, to.index)
  }

  val slideableController = LocalSlideableController.current
  LaunchedEffect(lazyListState) {
    snapshotFlow { lazyListState.isScrollInProgress }
      .distinctUntilChanged()
      .collect {
        if (it) {
          offsets.clear()
          slideableController.emit(SlideableListEvent.ScrollStarted)
        }
      }
  }

  LazyColumn(
    state = lazyListState,
    modifier = modifier
      .screenUnderTopBarPaddings()
      .background(MaterialTheme.colorScheme.surface)
  ) {
    itemsIndexed(
      items = items,
      key = { _, item -> item.key }
    ) { index, item ->
      ReorderableItem(
        state = reorderableLazyListState,
        key = item.key,
        enabled = item.draggable
      ) { isDragging ->
        when (item) {
          is ListItem.DefaultItem -> {
            val context = LocalContext.current
            DefaultItemView(
              item = item,
              offsets = offsets,
              isDragging = isDragging,
              onLeftButtonClick = { onLeftButtonClick(item.remoteId) },
              onRightButtonClick = { onRightButtonClick(item.remoteId) },
              onDragStopped = { onDragStopped(item.remoteId) },
              onInfoClick = { onInfoClick(item.remoteId) },
              onIssueClick = { onIssueClick(item.issues.message(context)) },
              onItemClick = { onItemClick(item.remoteId) },
              onTitleLongClick = { onTitleLongClick(item) }
            )
          }
          is ListItem.LocationItem ->
            LocationListItemView(
              caption = item.userCaption,
              collapsed = item.collapsed,
              onClick = { onLocationClick(item.remoteId) },
              onLongClick = { onLocationLongClick(item) },
              modifier = if (index == 0) Modifier.topSearchBarPaddings() else Modifier
            )
          is ListItem.SceneItem ->
            SlideableListItem(
              objectId = item.remoteId,
              initialOffset = offsets[item.remoteId] ?: 0f,
              onOffsetChanged = {
                offsets.clear()
                offsets[item.remoteId] = it
              },
              isDragging = isDragging,
              onLeftButtonClick = { onLeftButtonClick(item.remoteId) },
              onRightButtonClick = { onRightButtonClick(item.remoteId) },
              onDragStopped = { onDragStopped(item.remoteId) },
              leftButtonString = item.status.online.forTrue { localizedString(R.string.btn_abort) },
              rightButtonString = item.status.online.forTrue { localizedString(R.string.btn_execute) }
            ) {
              SceneListItemView(
                data = item,
                onItemClick = { onItemClick(item.remoteId) },
                onTitleLongClick = { onTitleLongClick(item) }
              )
            }
        }
      }
    }
  }
}

@Composable
fun ReorderableCollectionItemScope.DefaultItemView(
  item: ListItem.DefaultItem,
  offsets: SnapshotStateMap<Int, Float>,
  isDragging: Boolean,
  onLeftButtonClick: () -> Unit = {},
  onRightButtonClick: () -> Unit = {},
  onDragStopped: () -> Unit = {},
  onInfoClick: () -> Unit = {},
  onIssueClick: (ListItemIssues) -> Unit = {},
  onItemClick: () -> Unit = {},
  onTitleLongClick: () -> Unit = {},
) {
  SlideableListItem(
    objectId = item.remoteId,
    initialOffset = offsets[item.remoteId] ?: 0f,
    onOffsetChanged = {
      offsets.clear()
      offsets[item.remoteId] = it
    },
    isDragging = isDragging,
    onLeftButtonClick = onLeftButtonClick,
    onRightButtonClick = onRightButtonClick,
    onDragStopped = onDragStopped,
    leftButtonString = item.status.online.forTrue { item.leftButtonString },
    rightButtonString = item.status.online.forTrue { item.rightButtonString }
  ) {
    when (item) {
      is ListItem.HvacThermostatItem ->
        ThermostatListItemView(
          data = item,
          onInfoClick = onInfoClick,
          onIssueClick = onIssueClick,
          onItemClick = onItemClick,
          onTitleLongClick = onTitleLongClick
        )
      is ListItem.HeatpolThermostatItem ->
        HeatpolThermostatListItemView(
          data = item,
          onInfoClick = onInfoClick,
          onIssueClick = onIssueClick,
          onItemClick = onItemClick,
          onTitleLongClick = onTitleLongClick
        )
      is ListItem.DoubleValueItem ->
        DoubleIconValueListItemView(
          data = item,
          onInfoClick = onInfoClick,
          onIssueClick = onIssueClick,
          onItemClick = onItemClick,
          onTitleLongClick = onTitleLongClick
        )
      is ListItem.DefaultItem ->
        IconValueListItemView(
          data = item,
          onInfoClick = onInfoClick,
          onIssueClick = onIssueClick,
          onItemClick = onItemClick,
          onTitleLongClick = onTitleLongClick
        )
    }
  }
}
