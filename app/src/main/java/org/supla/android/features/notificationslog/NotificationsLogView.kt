@file:OptIn(ExperimentalMaterialApi::class)

package org.supla.android.features.notificationslog
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
syays GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FixedThreshold
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.supla.android.R
import org.supla.android.core.ui.BaseViewProxy
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.local.entity.NotificationEntity
import org.supla.android.ui.dialogs.AlertDialog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface NotificationsLogViewProxy : BaseViewProxy<NotificationsLogViewState> {
  fun delete(entity: NotificationEntity) {}
  fun cancelDeletion(id: Long) {}
  fun askDeleteAll() {}
  fun cancelDeleteAll() {}
  fun deleteAll() {}
}

@Composable
fun NotificationsLogView(viewProxy: NotificationsLogViewProxy) {
  val viewState by viewProxy.getViewState().collectAsState()

  if (viewState.showDeletionDialog) {
    AlertDialog(
      title = stringResource(id = R.string.notification_delete_all_title),
      message = stringResource(id = R.string.notification_delete_all_message),
      positiveButtonTitle = stringResource(id = R.string.notification_delete_all_proceed),
      onNegativeClick = { viewProxy.cancelDeleteAll() },
      onPositiveClick = { viewProxy.deleteAll() }
    )
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colors.background)
      .padding(top = 1.dp)
  ) {
    items(
      items = viewState.items,
      key = { it.notificationEntity.id },
      itemContent = { item ->
        val currentItem by rememberUpdatedState(item)
        val dismissState = rememberDismissState(
          confirmStateChange = {
            if (it == DismissValue.DismissedToEnd) {
              viewProxy.delete(currentItem.notificationEntity)
              return@rememberDismissState true
            }
            false
          }
        )
        LaunchedEffect(Any()) { dismissState.reset() }
        if (item.deleted.not()) {
          SwipeToDismiss(
            state = dismissState,
            directions = setOf(DismissDirection.StartToEnd),
            dismissThresholds = { FixedThreshold(112.dp) },
            background = { NotificationRowBackground() }
          ) {
            NotificationRow(item.notificationEntity)
          }
        }
      }
    )
  }
}

@Composable
private fun NotificationRow(entity: NotificationEntity) {
  val formatter by remember { mutableStateOf(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss")) }
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 1.dp)
      .background(MaterialTheme.colors.surface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(all = Distance.tiny),
      horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.End)
    ) {
      entity.profileName?.let {
        Text(
          text = stringResource(id = R.string.notifications_log_profile),
          style = MaterialTheme.typography.caption,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = entity.profileName,
          style = MaterialTheme.typography.caption,
          modifier = Modifier.padding(end = Distance.tiny)
        )
      }
      Text(
        text = stringResource(id = R.string.notifications_log_date),
        style = MaterialTheme.typography.caption,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = formatter.format(entity.date),
        style = MaterialTheme.typography.caption
      )
    }
    Text(
      text = entity.title,
      style = MaterialTheme.typography.h6,
      modifier = Modifier.padding(start = Distance.default, end = Distance.default)
    )
    Text(
      text = entity.message,
      style = MaterialTheme.typography.body2,
      modifier = Modifier.padding(start = Distance.default, top = Distance.tiny, end = Distance.default, bottom = Distance.small)
    )
  }
}

@Composable
private fun NotificationRowBackground() =
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .padding(bottom = 1.dp)
      .background(MaterialTheme.colors.error)
  ) {
    Icon(
      painter = painterResource(id = R.drawable.ic_delete),
      contentDescription = null,
      tint = MaterialTheme.colors.onPrimary,
      modifier = Modifier
        .align(Alignment.CenterStart)
        .padding(all = Distance.default)
    )
  }

@Composable
@Preview
private fun Preview() {
  SuplaTheme {
    NotificationsLogView(PreviewProxy())
  }
}

private class PreviewProxy : NotificationsLogViewProxy {
  override fun getViewState(): StateFlow<NotificationsLogViewState> =
    MutableStateFlow(
      value = NotificationsLogViewState(
        items = listOf(
          NotificationItem(
            NotificationEntity(
              title = "Some notification title",
              message = "Some notification message",
              profileName = "Default",
              date = LocalDateTime.of(2023, 11, 10, 22, 3, 14)
            ),
            false
          )
        )
      )
    )
}
