package org.supla.android.features.details.thermostatdetail.timer
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
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.databinding.FragmentComposeBinding
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.features.details.thermostatdetail.timer.ui.ThermostatTimerDetail
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage

private const val ARG_ITEM_BUNDLE = "ARG_ITEM_BUNDLE"

@AndroidEntryPoint
class TimerDetailFragment : BaseFragment<TimerDetailViewState, TimerDetailViewEvent>(R.layout.fragment_compose) {

  override val viewModel: TimerDetailViewModel by viewModels()
  private val binding by viewBinding(FragmentComposeBinding::bind)

  private val item: ItemBundle by lazy { requireSerializable(ARG_ITEM_BUNDLE, ItemBundle::class.java) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.composeContent.setContent {
      SuplaTheme {
        ThermostatTimerDetail(viewModel)
      }
    }

    viewModel.observeData(item.remoteId)
  }

  override fun onResume() {
    super.onResume()
    viewModel.loadData(item.remoteId)
  }

  override fun handleEvents(event: TimerDetailViewEvent) {
  }

  override fun handleViewState(state: TimerDetailViewState) {
  }

  override fun onSuplaMessage(message: SuplaClientMessage) {
    (message as? SuplaClientMessage.ChannelDataChanged)?.let {
      if (it.channelId == item.remoteId && (it.timerValueChanged || !it.extendedValueChanged)) {
        viewModel.loadData(item.remoteId)
      }
    }
  }

  companion object {
    fun bundle(item: ItemBundle) = bundleOf(ARG_ITEM_BUNDLE to item)
  }
}

enum class DeviceMode(val position: Int, @StringRes val stringRes: Int) {
  OFF(0, R.string.turn_off),
  MANUAL(1, R.string.details_timer_manual_mode);

  companion object {
    fun from(idx: Int): DeviceMode {
      entries.forEachIndexed { index, deviceMode ->
        if (idx == index) {
          return deviceMode
        }
      }

      throw IllegalArgumentException("Device Mode for idx `$idx` not found")
    }
  }
}
