package org.supla.android.features.standarddetail.switchdetail
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
import android.text.format.DateFormat
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.databinding.FragmentSwitchDetailBinding
import org.supla.android.db.ChannelBase
import org.supla.android.images.ImageCache.getBitmap
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.model.ItemType

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"
private const val ARG_ITEM_TYPE = "ARG_ITEM_TYPE"

@AndroidEntryPoint
class SwitchDetailFragment : BaseFragment<SwitchDetailViewState, SwitchDetailViewEvent>(R.layout.fragment_switch_detail) {

  private val viewModel: SwitchDetailViewModel by viewModels()
  private val binding by viewBinding(FragmentSwitchDetailBinding::bind)

  private val itemType: ItemType by lazy { arguments!!.getSerializable(ARG_ITEM_TYPE) as ItemType }
  private val remoteId: Int by lazy { arguments!!.getInt(ARG_REMOTE_ID) }

  override fun getViewModel(): BaseViewModel<SwitchDetailViewState, SwitchDetailViewEvent> = viewModel

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.switchDetailButtonOn.clickListener = { viewModel.turnOn(remoteId, itemType) }
    binding.switchDetailButtonOff.clickListener = { viewModel.turnOff(remoteId, itemType) }
    viewModel.loadData(remoteId, itemType)
  }

  override fun handleEvents(event: SwitchDetailViewEvent) {
    when (event) {
      SwitchDetailViewEvent.Close -> requireActivity().finish()
    }
  }

  override fun handleViewState(state: SwitchDetailViewState) {
    state.channelBase?.let {
      binding.switchDetailDeviceState.deviceStateIcon.setImageBitmap(getBitmap(context, it.imageIdx))
      binding.switchDetailButtonOn.disabled = it.onLine.not()
      binding.switchDetailButtonOn.icon = getBitmap(context, it.getImageIdx(false, ChannelBase.WhichOne.First, 1))
      binding.switchDetailButtonOff.disabled = it.onLine.not()
      binding.switchDetailButtonOff.icon = getBitmap(context, it.getImageIdx(false, ChannelBase.WhichOne.First, 0))
    }
    binding.switchDetailDeviceState.deviceStateLabel.text = if (state.timerEndDate != null) {
      val formatString = getString(R.string.hour_string_format)
      getString(R.string.details_timer_state_label_for_timer, DateFormat.format(formatString, state.timerEndDate))
    } else {
      getString(R.string.details_timer_state_label)
    }
    binding.switchDetailDeviceState.deviceStateValue.text = when {
      state.channelBase?.onLine?.not() == true -> getString(R.string.offline)
      state.isOn -> getString(R.string.details_timer_device_on)
      else -> getString(R.string.details_timer_device_off)
    }
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    when (message.type) {
      SuplaClientMsg.onDataChanged -> {
        if (message.channelId == remoteId && (message.isTimerValue || !message.isExtendedValue)) {
          viewModel.loadData(remoteId, itemType)
        }
      }
    }
  }

  companion object {
    fun bundle(remoteId: Int, itemType: ItemType) = bundleOf(
      ARG_REMOTE_ID to remoteId,
      ARG_ITEM_TYPE to itemType
    )
  }
}
