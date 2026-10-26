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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.captionchangedialog.CaptionChangeDialogScope
import org.supla.android.features.captionchangedialog.CaptionChangeDialogState
import org.supla.android.features.captionchangedialog.Dialog
import org.supla.android.images.ImageId
import org.supla.android.main.scaffold.screenPaddings
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.dialogs.AuthorizationDialog
import org.supla.android.ui.dialogs.AuthorizationDialogScope
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.views.ErrorContent
import org.supla.android.ui.views.settings.SettingCheckboxItem
import org.supla.android.ui.views.settings.SettingsBlock
import org.supla.android.usecases.schedule.ScheduleVo
import org.supla.core.shared.infrastructure.LocalizedString

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
      } else {
        viewState.items?.let { List(it) }
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
  SettingsBlock(
    items = items,
    modifier = Modifier.verticalScroll(state = rememberScrollState())
  ) { item ->
    SettingCheckboxItem(
      label = item.caption(),
      checked = item.enabled,
      onLabelLongClick = { onTitleLongClick(item) },
      onCheckedChanged = { onEnableChanged(item.remoteId, it) }
    )
  }
}

@SuplaPreview
@Composable
private fun ScheduleListViewPreview() {
  SuplaTheme {
    PreviewScheduleListScope.View(
      ScheduleListViewState(
        items = listOf(
          sampleSchedule(1, "Living room lights", false),
          sampleSchedule(2, "Close the blinds"),
          sampleSchedule(3, "Heating", false)
        ),
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

private fun sampleSchedule(id: Int, caption: String, enabled: Boolean = true) = ScheduleVo(
  remoteId = id,
  profileId = 1,
  userCaption = caption,
  caption = LocalizedString.Constant(caption),
  icon = ImageId(R.drawable.fnc_gpm_5),
  enabled = enabled
)
