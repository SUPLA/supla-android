package org.supla.android.features.details.electricitymeterdetail.history
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.features.details.detailbase.history.BaseHistoryDetailFragment
import org.supla.android.features.details.detailbase.history.HistoryDetailViewState

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"

@AndroidEntryPoint
class ElectricityMeterHistoryFragment : BaseHistoryDetailFragment() {

  override val viewModel: ElectricityMeterHistoryViewModel by viewModels()

  @Composable
  override fun Content(viewState: HistoryDetailViewState) {
    Box(modifier = Modifier.fillMaxSize()) {
      super.Content(viewState)

      viewState.introductionPages?.let {
        ElectricityMeterHistoryIntroduction(it) {
          viewModel.closeIntroduction()
        }
      }
    }
  }

  companion object {
    fun bundle(remoteId: Int) = bundleOf(ARG_REMOTE_ID to remoteId)
  }
}
