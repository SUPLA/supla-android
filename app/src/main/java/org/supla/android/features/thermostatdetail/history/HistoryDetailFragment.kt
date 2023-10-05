package org.supla.android.features.thermostatdetail.history
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
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.databinding.FragmentComposeBinding
import org.supla.android.features.thermostatdetail.history.ui.HistoryDetail

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"

@AndroidEntryPoint
class HistoryDetailFragment : BaseFragment<HistoryDetailViewState, HistoryDetailViewEvent>(R.layout.fragment_compose) {

  override val viewModel: HistoryDetailViewModel by viewModels()
  private val binding by viewBinding(FragmentComposeBinding::bind)

  private val remoteId: Int by lazy { requireArguments().getInt(ARG_REMOTE_ID) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.loadData(remoteId)

    binding.composeContent.setContent {
      SuplaTheme {
        HistoryDetail(viewModel)
      }
    }
  }

  override fun handleEvents(event: HistoryDetailViewEvent) {
  }

  override fun handleViewState(state: HistoryDetailViewState) {
  }

  companion object {
    fun bundle(remoteId: Int) = bundleOf(ARG_REMOTE_ID to remoteId)
  }
}
