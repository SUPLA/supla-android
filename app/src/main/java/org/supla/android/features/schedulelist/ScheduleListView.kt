package org.supla.android.features.schedulelist
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

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.formatting.DateFormatter
import org.supla.android.data.formatting.LocalDateFormatter
import org.supla.android.extensions.suplaCard
import org.supla.android.features.captionchangedialog.CaptionChangeDialogScope
import org.supla.android.features.captionchangedialog.CaptionChangeDialogState
import org.supla.android.features.captionchangedialog.Dialog
import org.supla.android.images.ImageId
import org.supla.android.main.scaffold.screenPaddings
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.dialogs.AuthorizationDialog
import org.supla.android.ui.dialogs.AuthorizationDialogScope
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.views.EmptyListInfoView
import org.supla.android.ui.views.ErrorContent
import org.supla.android.ui.views.Switch
import org.supla.android.ui.views.settings.SettingDescription
import org.supla.android.ui.views.settings.SettingLabel
import org.supla.android.ui.views.settings.SettingRow
import org.supla.android.ui.views.settings.SettingsBlock
import org.supla.android.usecases.schedule.ScheduleExecutionVo
import org.supla.android.usecases.schedule.ScheduleVo
import org.supla.core.shared.infrastructure.LocalizedString
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

interface ScheduleListScope : AuthorizationDialogScope, CaptionChangeDialogScope {
  fun onEnableChanged(remoteId: Int, enabled: Boolean)
  fun onTitleLongClick(schedule: ScheduleVo)
  fun onTryAgain()
  fun onRefresh()
}

@Composable
fun ScheduleListScope.View(viewState: ScheduleListViewState) {
  val pullToRefreshState = rememberPullToRefreshState()

  Box(
    modifier = Modifier
      .screenPaddings()
      .fillMaxSize()
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .pullToRefresh(
          isRefreshing = viewState.loading,
          onRefresh = ::onRefresh,
          state = pullToRefreshState,
        )
    ) {
      if (viewState.loadingError) {
        ErrorContent(
          message = R.string.status_unknown_err,
          onTryAgainClick = ::onTryAgain
        )
      } else if (viewState.items == null && !viewState.loading) {
        ErrorContent(
          message = R.string.schedules_login_required,
          onTryAgainClick = ::onTryAgain
        )
      } else if (viewState.items.isNullOrEmpty()) {
        EmptyListInfoView(modifier = Modifier.align(Alignment.Center))
      } else {
        List(viewState.items)
      }

      PullToRefreshDefaults.Indicator(
        state = pullToRefreshState,
        isRefreshing = viewState.loading,
        modifier = Modifier.align(Alignment.TopCenter),
        color = MaterialTheme.colorScheme.primary,
      )
    }

    viewState.captionChangeDialogState?.let { Dialog(it) }
    viewState.authorizationDialogState?.let { AuthorizationDialog(it) }
  }
}

@Composable
private fun ScheduleListScope.List(items: List<ScheduleVo>) {
  val dateFormatter = LocalDateFormatter.current
  SettingsBlock(
    items = items,
    modifier = Modifier
      .verticalScroll(state = rememberScrollState())
      .padding(vertical = Distance.tiny)
  ) { item ->
    SettingRow(horizontalArrangement = Arrangement.spacedBy(Distance.default)) {
      Column(
        modifier = Modifier
          .weight(1f)
          .then(if (item.lastExecution != null || item.nextExecution != null) Modifier.padding(bottom = Distance.tiny) else Modifier)
      ) {
        SettingLabel(
          label = item.caption(),
          modifier = Modifier
            .combinedClickable(
              enabled = true,
              onClick = {},
              onLongClick = { onTitleLongClick(item) }
            )
            .then(if (item.lastExecution != null || item.nextExecution != null) Modifier.padding(bottom = Distance.tiny) else Modifier)
        )
        item.lastExecution?.let {
          ScheduleExecutionView(it, R.string.schedule_last_execution, dateFormatter, showResult = true)
        }
        item.nextExecution?.let {
          ScheduleExecutionView(it, R.string.schedule_next_execution, dateFormatter)
        }
      }

      Switch(
        checked = item.enabled,
        onCheckedChange = { onEnableChanged(item.remoteId, it) }
      )
    }
  }
}

@Composable
private fun ScheduleExecutionView(
  execution: ScheduleExecutionVo,
  labelRes: Int,
  dateFormatter: DateFormatter,
  showResult: Boolean = false
) {
  val timestamp = execution.plannedTimestamp ?: return

  Row(
    horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
  ) {
    SettingDescription(stringResource(labelRes, formatScheduleTimestamp(timestamp, dateFormatter)))
    if (showResult) {
      execution.failed?.let { failed ->
        Text(
          text = stringResource(if (failed) R.string.notifications_inactive else R.string.notifications_active),
          style = MaterialTheme.typography.bodySmall,
          color = if (failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}

private fun formatScheduleTimestamp(timestamp: LocalDateTime, dateFormatter: DateFormatter): String =
  dateFormatter.getFullDateString(Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant())) ?: ""

@SuplaPreview
@Composable
private fun ScheduleListViewPreview() {
  SuplaTheme {
    PreviewScheduleListScope.View(
      ScheduleListViewState(
        items = listOf(
          sampleSchedule(
            1,
            "Living room lights",
            false,
            lastExecution = ScheduleExecutionVo(LocalDateTime.of(2026, 10, 25, 12, 0), true),
            nextExecution = ScheduleExecutionVo(LocalDateTime.of(2026, 10, 27, 12, 0), null)
          ),
          sampleSchedule(
            2,
            "Close the blinds",
            lastExecution = ScheduleExecutionVo(LocalDateTime.of(2026, 10, 26, 8, 30), false),
            nextExecution = ScheduleExecutionVo(LocalDateTime.of(2026, 10, 27, 8, 30), null)
          ),
          sampleSchedule(3, "Heating", false)
        ),
        loading = false
      )
    )
  }
}

@SuplaPreview
@Composable
private fun PreviewEmpty() {
  SuplaTheme {
    PreviewScheduleListScope.View(
      ScheduleListViewState(
        items = listOf(),
        loading = false
      )
    )
  }
}

private object PreviewScheduleListScope : ScheduleListScope {
  override fun onEnableChanged(remoteId: Int, enabled: Boolean) {}
  override fun onTitleLongClick(schedule: ScheduleVo) {}
  override fun onAuthorizationDismiss() {}
  override fun onAuthorizationCancel() {}
  override fun onAuthorize(userName: String, password: String) {}
  override fun onStateChange(state: AuthorizationDialogState) {}
  override fun onTryAgain() {}
  override fun onRefresh() {}
  override fun onCaptionChangeDismiss() {}
  override fun onStateChange(state: CaptionChangeDialogState) {}
  override fun onCaptionChangeConfirmed() {}
}

private fun sampleSchedule(
  id: Int,
  caption: String,
  enabled: Boolean = true,
  lastExecution: ScheduleExecutionVo? = null,
  nextExecution: ScheduleExecutionVo? = null
) = ScheduleVo(
  remoteId = id,
  profileId = 1,
  userCaption = caption,
  caption = LocalizedString.Constant(caption),
  enabled = enabled,
  lastExecution = lastExecution,
  nextExecution = nextExecution
)
