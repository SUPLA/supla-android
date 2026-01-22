package org.supla.android.features.details.impulsecounter.general
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
import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.detailbase.impulsecounter.ImpulseCounterMetricsView
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage

@AndroidEntryPoint
class ImpulseCounterGeneralFragment : BaseComposeFragment<ImpulseCounterGeneralViewModelState, ImpulseCounterGeneralViewEvent>() {

  override val viewModel: ImpulseCounterGeneralViewModel by viewModels()

  @Composable
  override fun ComposableContent(modelState: ImpulseCounterGeneralViewModelState) {
    SuplaTheme {
      ImpulseCounterMetricsView(modelState.viewState)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.onViewCreated(item.remoteId)
  }

  override fun onStart() {
    super.onStart()

    viewModel.loadData(item.remoteId)
  }

  override fun handleEvents(event: ImpulseCounterGeneralViewEvent) {
  }

  override fun onSuplaMessage(message: SuplaClientMessage) {
    (message as? SuplaClientMessage.ChannelDataChanged)?.let {
      if (it.channelId == item.remoteId) {
        viewModel.loadData(item.remoteId)
      }
    }
  }
}
