package org.supla.android.features.details.detailbase.history
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
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.supla.android.R
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.features.details.detailbase.history.ui.View
import org.supla.android.main.ViewModelHost
import org.supla.android.main.topbar.RegisterTopBarIcon
import org.supla.android.main.topbar.TopBarIcon

@Composable
fun HistoryDetailScreen(
  item: ItemBundle,
  viewModel: BaseHistoryDetailViewModel,
  popup: @Composable (HistoryDetailViewState) -> Unit = {}
) {
  val context = LocalContext.current

  ViewModelHost(
    viewModel = viewModel,
    onResume = { viewModel.loadData(item.remoteId) },
    eventHandler = { handleEvent(it, context) }
  ) {
    RegisterTopBarIcon(TopBarIcon.ReloadHistory) { viewModel.deleteAndDownloadData(item.remoteId) }

    viewModel.View(it)

    popup(it)
  }
}

private fun handleEvent(event: HistoryDetailViewEvent, context: Context) {
  when (event) {
    HistoryDetailViewEvent.ShowDownloadInProgressToast ->
      Toast.makeText(context, R.string.history_wait_for_download_completed, Toast.LENGTH_SHORT).show()
  }
}
