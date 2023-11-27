package org.supla.android.features.switchdetail.switchgeneral
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
import org.supla.android.databinding.FragmentSwitchDetailBinding
import org.supla.android.db.ChannelBase
import org.supla.android.features.standarddetail.ItemBundle
import org.supla.android.images.ImageCache.getBitmap
import org.supla.android.lib.SuplaClientMsg

private const val ARG_ITEM_BUNDLE = "ARG_ITEM_BUNDLE"

@AndroidEntryPoint
class SwitchGeneralFragment : BaseFragment<SwitchGeneralViewState, SwitchGeneralViewEvent>(R.layout.fragment_switch_detail) {

  override val viewModel: SwitchGeneralViewModel by viewModels()
  private val binding by viewBinding(FragmentSwitchDetailBinding::bind)

  private val item: ItemBundle by lazy { requireSerializable(ARG_ITEM_BUNDLE, ItemBundle::class.java) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.switchDetailButtonOn.clickListener = { viewModel.turnOn(item.remoteId, item.itemType) }
    binding.switchDetailButtonOff.clickListener = { viewModel.turnOff(item.remoteId, item.itemType) }
  }

  override fun onResume() {
    super.onResume()
    viewModel.loadData(item.remoteId, item.itemType)
  }

  override fun handleEvents(event: SwitchGeneralViewEvent) {
  }

  override fun handleViewState(state: SwitchGeneralViewState) {
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
        if (message.channelId == item.remoteId && (message.isTimerValue || !message.isExtendedValue)) {
          viewModel.loadData(item.remoteId, item.itemType)
        }
      }
    }
  }

  companion object {
    fun bundle(itemBundle: ItemBundle) = bundleOf(
      ARG_ITEM_BUNDLE to itemBundle
    )
  }
}
