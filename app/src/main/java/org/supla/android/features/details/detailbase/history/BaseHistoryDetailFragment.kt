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

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.databinding.FragmentComposeBinding
import org.supla.android.features.details.detailbase.history.ui.HistoryDetail
import org.supla.android.ui.ToolbarItemsClickHandler

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"

abstract class BaseHistoryDetailFragment :
  BaseFragment<HistoryDetailViewState, HistoryDetailViewEvent>(R.layout.fragment_compose),
  ToolbarItemsClickHandler {

  abstract override val viewModel: BaseHistoryDetailViewModel
  private val binding by viewBinding(FragmentComposeBinding::bind)

  protected val remoteId: Int by lazy { requireArguments().getInt(ARG_REMOTE_ID) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.composeContent.setContent {
      val viewState by viewModel.getViewState().collectAsState()
      SuplaTheme {
        Content(viewState)
      }
    }
  }

  @Composable
  open fun Content(viewState: HistoryDetailViewState) {
    HistoryDetail(viewModel, viewState)
  }

  override fun onResume() {
    super.onResume()
    viewModel.loadData(remoteId)
    setToolbarItemVisible(R.id.toolbar_delete_chart_history, true)
  }

  override fun onPause() {
    super.onPause()
    setToolbarItemVisible(R.id.toolbar_delete_chart_history, false)
  }

  override fun handleEvents(event: HistoryDetailViewEvent) {
    when (event) {
      is HistoryDetailViewEvent.ShowDownloadInProgressToast ->
        Toast.makeText(requireContext(), R.string.history_wait_for_download_completed, Toast.LENGTH_SHORT).show()
    }
  }

  override fun handleViewState(state: HistoryDetailViewState) {
  }

  override fun onMenuItemClick(menuItem: MenuItem): Boolean {
    if (menuItem.itemId == R.id.toolbar_delete_chart_history) {
      viewModel.deleteAndDownloadData(remoteId)
      return true
    }

    return false
  }
}
