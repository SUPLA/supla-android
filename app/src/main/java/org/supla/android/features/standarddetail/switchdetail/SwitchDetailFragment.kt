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
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.databinding.FragmentSwitchDetailBinding
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
    binding.switchDetailButtonOn.setOnClickListener { viewModel.turnOn(remoteId, itemType) }
    binding.switchDetailButtonOff.setOnClickListener { viewModel.turnOff(remoteId, itemType) }
    viewModel.loadData(remoteId, itemType)
  }

  override fun handleEvents(event: SwitchDetailViewEvent) {
  }

  override fun handleViewState(state: SwitchDetailViewState) {
    state.imageId?.let {
      binding.switchDetailDeviceState.deviceStateIcon.setImageBitmap(getBitmap(context, it))
    }
    binding.switchDetailDeviceState.deviceStateValue.text = if (state.isOn) {
      getString(R.string.details_timer_device_on)
    } else {
      getString(R.string.details_timer_device_off)
    }
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    when (message.type) {
      SuplaClientMsg.onDataChanged -> {
        if (message.channelId == remoteId) {
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
