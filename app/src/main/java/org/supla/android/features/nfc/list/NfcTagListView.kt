package org.supla.android.features.nfc.list
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.buttons.FloatingAddButton
import org.supla.android.ui.views.list.components.ListItemIcon
import org.supla.android.ui.views.list.components.ListItemProfile
import org.supla.android.ui.views.list.components.ListItemTitle
import java.util.UUID

data class NfcTagListViewState(
  val loading: Boolean = false,
  val items: List<NfcTagItem> = emptyList()
)

data class NfcTagItem(
  val id: Long,
  val uuid: String,
  val name: String,
  val icon: ImageId?,
  val profileName: String?
)

interface NfcTagListScope {
  fun onAddClick()
  fun onItemClick(item: NfcTagItem)
}

@Composable
fun NfcTagListScope.View(viewState: NfcTagListViewState) {
  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      state = rememberLazyListState(),
      modifier = Modifier.fillMaxSize()
    ) {
      items(
        items = viewState.items,
        key = { it.id },
        itemContent = { item ->
          ItemView(
            item = item,
            onItemClick = { onItemClick(item) },
          )
        }
      )
    }

    AddButton(modifier = Modifier.align(Alignment.BottomEnd))
  }
}

@Composable
private fun NfcTagListScope.AddButton(modifier: Modifier = Modifier) =
  FloatingAddButton(
    modifier = modifier.padding(Distance.default),
    contentDescription = stringResource(R.string.nfc_list_add),
    onClick = { onAddClick() }
  )

@Composable
private fun ItemView(
  item: NfcTagItem,
  onItemClick: () -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 1.dp)
      .background(MaterialTheme.colorScheme.surface)
      .clickable(onClick = onItemClick)
      .padding(start = Distance.small, end = Distance.default)
      .padding(vertical = Distance.small)
  ) {
    item.icon?.let { ListItemIcon(imageId = it, scale = 1f) }
    Column(
      modifier = Modifier
        .padding(start = Distance.tiny)
        .weight(1f),
      verticalArrangement = Arrangement.spacedBy(Distance.tiny)
    ) {
      ListItemTitle(
        text = item.name,
        onItemClick = onItemClick,
        onLongClick = onItemClick,
        modifier = Modifier,
        maxLines = 2
      )

      Text(
        text = item.uuid,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    item.profileName?.let {
      ListItemProfile(it, modifier = Modifier.align(Alignment.Top))
    }
  }
}

private val previewScope = object : NfcTagListScope {
  override fun onAddClick() {}
  override fun onItemClick(item: NfcTagItem) {}
}

@SuplaPreview
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      viewState = NfcTagListViewState(
        items = listOf(
          item(0, "Tag 1"),
          item(1, "Tag 2"),
          item(2, "Tag 3")
        )
      )
    )
  }
}

private fun item(id: Long, name: String): NfcTagItem =
  NfcTagItem(id = id, uuid = UUID.randomUUID().toString(), name = name, icon = null, profileName = null)
