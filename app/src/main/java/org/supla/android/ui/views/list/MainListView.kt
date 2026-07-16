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

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import org.supla.android.R
import org.supla.android.main.scaffold.screenPaddings
import org.supla.android.main.scaffold.screenUnderTopBarPaddings
import org.supla.android.main.topbar.LocalTopBarController
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
import org.supla.core.shared.extensions.forTrue
import org.supla.core.shared.infrastructure.localizedString
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

interface MainListScope {
  fun onLeftButtonClick(remoteId: Int)
  fun onRightButtonClick(remoteId: Int)
  fun moveItems(from: Int, to: Int): Boolean
  fun onDragStarted(remoteId: Int)
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
fun MainListScope.Content(
  items: List<ListItem>,
  listState: LazyListState,
  modifier: Modifier = Modifier,
  emptyContent: @Composable BoxScope.() -> Unit = {}
) {
  if (items.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .screenUnderTopBarPaddings()
    ) {
      emptyContent()
    }
  } else {
    ListView(
      items = items,
      listState = listState,
      modifier = modifier
    )
  }
}

@Composable
private fun MainListScope.ListView(
  items: List<ListItem>,
  listState: LazyListState,
  modifier: Modifier = Modifier
) {
  val offsets = remember { mutableStateMapOf<Int, Float>() }
  val reorderableLazyListState = rememberReorderableLazyListState(
    lazyListState = listState
  ) { from, to ->
    moveItems(from.index, to.index)
  }

  // LaunchedEffect below is used to hide list item buttons
  val slideableController = LocalSlideableController.current
  LaunchedEffect(listState) {
    snapshotFlow { listState.isScrollInProgress }
      .distinctUntilChanged()
      .collect {
        if (it) {
          offsets.clear()
          slideableController.emit(SlideableListEvent.ScrollStarted)
        }
      }
  }

  LazyColumn(
    state = listState,
    modifier = modifier
      .screenPaddings()
      .background(MaterialTheme.colorScheme.outline),
    verticalArrangement = Arrangement.spacedBy(1.dp)
  ) {
    items(
      items = items,
      key = { item -> item.key }
    ) { item ->
      ReorderableItem(
        state = reorderableLazyListState,
        key = item.key,
        enabled = item.draggable,
        animateItemModifier = Modifier
      ) { isDragging ->
        when (item) {
          is ListItem.DefaultItem -> {
            DefaultItemView(
              item = item,
              offsets = offsets,
              isDragging = isDragging,
              dragEnabled = !LocalTopBarController.current.searchFilterSet,
              listScope = this@ListView
            )
          }
          is ListItem.LocationItem ->
            LocationListItemView(
              caption = item.userCaption,
              collapsed = item.collapsed,
              inSearch = LocalTopBarController.current.searchFilterSet,
              onClick = { onLocationClick(item.remoteId) },
              onLongClick = { onLocationLongClick(item) }
            )
          is ListItem.SceneItem ->
            SceneItemView(
              item = item,
              offsets = offsets,
              isDragging = isDragging,
              dragEnabled = !LocalTopBarController.current.searchFilterSet,
              listScope = this@ListView
            )
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
  dragEnabled: Boolean,
  listScope: MainListScope
) {
  SlideableListItem(
    objectId = item.remoteId,
    initialOffset = offsets[item.remoteId] ?: 0f,
    onOffsetChanged = {
      offsets.clear()
      offsets[item.remoteId] = it
    },
    isDragging = isDragging,
    dragEnabled = dragEnabled,
    onLeftButtonClick = { listScope.onLeftButtonClick(item.remoteId) },
    onRightButtonClick = { listScope.onRightButtonClick(item.remoteId) },
    onDragStarted = { listScope.onDragStarted(item.remoteId) },
    onDragStopped = { listScope.onDragStopped(item.remoteId) },
    leftButtonString = item.status.online.forTrue { item.leftButtonString },
    rightButtonString = item.status.online.forTrue { item.rightButtonString }
  ) {
    val context = LocalContext.current
    when (item) {
      is ListItem.HvacThermostatItem -> listScope.ThermostatListItemView(item, context)
      is ListItem.HeatpolThermostatItem -> listScope.HeatpolThermostatListItemView(item, context)
      is ListItem.DoubleValueItem -> listScope.DoubleIconValueListItemView(item, context)
      is ListItem.DefaultItem -> listScope.IconValueListItemView(item, context)
    }
  }
}

@Composable
fun MainListScope.ThermostatListItemView(item: ListItem.HvacThermostatItem, context: Context) =
  ThermostatListItemView(
    data = item,
    onInfoClick = { onInfoClick(item.remoteId) },
    onIssueClick = { onIssueClick(it.message(context)) },
    onItemClick = { onItemClick(item.remoteId) },
    onTitleLongClick = { onTitleLongClick(item) }
  )

@Composable
fun MainListScope.HeatpolThermostatListItemView(item: ListItem.HeatpolThermostatItem, context: Context) =
  HeatpolThermostatListItemView(
    data = item,
    onInfoClick = { onInfoClick(item.remoteId) },
    onIssueClick = { onIssueClick(it.message(context)) },
    onItemClick = { onItemClick(item.remoteId) },
    onTitleLongClick = { onTitleLongClick(item) }
  )

@Composable
fun MainListScope.DoubleIconValueListItemView(item: ListItem.DoubleValueItem, context: Context) =
  DoubleIconValueListItemView(
    data = item,
    onInfoClick = { onInfoClick(item.remoteId) },
    onIssueClick = { onIssueClick(it.message(context)) },
    onItemClick = { onItemClick(item.remoteId) },
    onTitleLongClick = { onTitleLongClick(item) }
  )

@Composable
fun MainListScope.IconValueListItemView(item: ListItem.DefaultItem, context: Context) =
  IconValueListItemView(
    data = item,
    onInfoClick = { onInfoClick(item.remoteId) },
    onIssueClick = { onIssueClick(it.message(context)) },
    onItemClick = { onItemClick(item.remoteId) },
    onTitleLongClick = { onTitleLongClick(item) }
  )

@Composable
fun ReorderableCollectionItemScope.SceneItemView(
  item: ListItem.SceneItem,
  offsets: SnapshotStateMap<Int, Float>,
  isDragging: Boolean,
  dragEnabled: Boolean,
  listScope: MainListScope
) {
  SlideableListItem(
    objectId = item.remoteId,
    initialOffset = offsets[item.remoteId] ?: 0f,
    onOffsetChanged = {
      offsets.clear()
      offsets[item.remoteId] = it
    },
    isDragging = isDragging,
    dragEnabled = dragEnabled,
    onLeftButtonClick = { listScope.onLeftButtonClick(item.remoteId) },
    onRightButtonClick = { listScope.onRightButtonClick(item.remoteId) },
    onDragStarted = { listScope.onDragStarted(item.remoteId) },
    onDragStopped = { listScope.onDragStopped(item.remoteId) },
    leftButtonString = item.status.online.forTrue { localizedString(R.string.btn_abort) },
    rightButtonString = item.status.online.forTrue { localizedString(R.string.btn_execute) }
  ) {
    SceneListItemView(
      data = item,
      onItemClick = { listScope.onItemClick(item.remoteId) },
      onTitleLongClick = { listScope.onTitleLongClick(item) }
    )
  }
}
