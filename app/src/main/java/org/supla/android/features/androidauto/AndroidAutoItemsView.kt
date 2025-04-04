package org.supla.android.features.androidauto
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.BACKGROUND_COLOR
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.list.components.ListItemIcon
import org.supla.android.ui.views.list.components.ListItemTitle

data class AndroidAutoItemsViewState(
  val items: List<AndroidAutoItem> = emptyList()
)

data class AndroidAutoItem(
  val id: Long,
  val subjectId: Int,
  val subjectType: SubjectType,
  val action: ActionId,
  val icon: ImageId?,
  val caption: String,
  val profileName: String
)

interface AndroidAutoItemsViewScope {
  fun onItemClick(item: AndroidAutoItem)
  fun onAddClick()
}

@Composable
fun AndroidAutoItemsViewScope.View(viewState: AndroidAutoItemsViewState) {
  Box(modifier = Modifier.fillMaxSize()) {
    if (viewState.items.isEmpty()) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.align(Alignment.Center)
      ) {
        Image(
          drawableId = R.drawable.ic_empty,
          modifier = Modifier.size(64.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = stringResource(R.string.main_no_entries),
          style = MaterialTheme.typography.headlineLarge,
          textAlign = TextAlign.Center
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
      ) {
        items(
          items = viewState.items,
          key = { it.id },
          itemContent = {
            ItemView(it, onItemClick = { onItemClick(it) })
          }
        )
      }
    }

    FloatingActionButton(
      onClick = { onAddClick() },
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(Distance.default)
    ) {
      Icon(
        painter = painterResource(R.drawable.ic_plus),
        contentDescription = stringResource(R.string.android_auto_add),
        tint = MaterialTheme.colorScheme.onPrimary
      )
    }
  }
}

@Composable
private fun ItemView(
  item: AndroidAutoItem,
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
        text = item.caption,
        onItemClick = onItemClick,
        onLongClick = onItemClick,
        modifier = Modifier,
        maxLines = 2
      )

      item.action.nameRes?.let {
        Text(
          text = stringResource(R.string.android_auto_action, stringResource(it)),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Column(
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.spacedBy(Distance.tiny)
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = stringResource(R.string.notifications_log_profile),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = item.profileName,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Text(
        text = stringResource(item.subjectType.nameRes),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

private val emptyScope = object : AndroidAutoItemsViewScope {
  override fun onItemClick(item: AndroidAutoItem) {}
  override fun onAddClick() {}
}

@Preview(showBackground = true, backgroundColor = BACKGROUND_COLOR)
@Composable
private fun Preview_List() {
  SuplaTheme {
    emptyScope.View(
      AndroidAutoItemsViewState(
        items = listOf(
          AndroidAutoItem(
            id = 1,
            subjectId = 1,
            subjectType = SubjectType.CHANNEL,
            action = ActionId.OPEN,
            profileName = "Default",
            icon = ImageId(R.drawable.fnc_thermostat_dhw),
            caption = "Thermostat"
          )
        )
      )
    )
  }
}

@Preview(showBackground = true, backgroundColor = BACKGROUND_COLOR)
@Composable
private fun Preview() {
  SuplaTheme {
    emptyScope.View(AndroidAutoItemsViewState())
  }
}
