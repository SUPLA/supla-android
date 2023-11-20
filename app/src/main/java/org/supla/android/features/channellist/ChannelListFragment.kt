package org.supla.android.features.channellist
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

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.ChannelStatePopup
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseFragment
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.databinding.FragmentChannelListBinding
import org.supla.android.db.Channel
import org.supla.android.extensions.toPx
import org.supla.android.features.switchdetail.SwitchDetailFragment
import org.supla.android.features.thermometerdetail.ThermometerDetailFragment
import org.supla.android.features.thermostatdetail.ThermostatDetailFragment
import org.supla.android.lib.SuplaChannelState
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.navigator.MainNavigator
import org.supla.android.ui.dialogs.exceededAmperageDialog
import org.supla.android.ui.dialogs.valveAlertDialog
import org.supla.android.usecases.channel.ButtonType
import javax.inject.Inject

@AndroidEntryPoint
class ChannelListFragment : BaseFragment<ChannelListViewState, ChannelListViewEvent>(R.layout.fragment_channel_list) {

  override val viewModel: ChannelListViewModel by viewModels()
  private val binding by viewBinding(FragmentChannelListBinding::bind)
  private lateinit var statePopup: ChannelStatePopup
  private var scrollDownOnReload = false

  @Inject
  lateinit var adapter: ChannelsAdapter

  @Inject
  lateinit var suplaClientProvider: SuplaClientProvider

  @Inject
  lateinit var navigator: MainNavigator

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.channelsList.adapter = adapter
    statePopup = ChannelStatePopup(requireActivity())
    setupAdapter()
  }

  override fun onStart() {
    super.onStart()
    viewModel.loadChannels()
  }

  override fun handleEvents(event: ChannelListViewEvent) {
    val suplaClient = suplaClientProvider.provide()
    when (event) {
      is ChannelListViewEvent.ShowValveDialog -> valveAlertDialog(event.remoteId, suplaClient).show()
      is ChannelListViewEvent.ShowAmperageExceededDialog -> exceededAmperageDialog(event.remoteId, suplaClient).show()
      is ChannelListViewEvent.OpenLegacyDetails -> {
        setToolbarTitle("")
        navigator.navigateToLegacyDetails(
          event.remoteId,
          event.type,
          ItemType.CHANNEL
        )
      }

      is ChannelListViewEvent.ReassignAdapter -> {
        binding.channelsList.adapter = null
        binding.channelsList.adapter = adapter
      }

      is ChannelListViewEvent.OpenSwitchDetail -> navigator.navigateTo(
        R.id.switch_detail_fragment,
        SwitchDetailFragment.bundle(event.itemBundle, event.pages.toTypedArray())
      )

      is ChannelListViewEvent.OpenThermostatDetail -> navigator.navigateTo(
        R.id.thermostat_detail_fragment,
        ThermostatDetailFragment.bundle(event.itemBundle, event.pages.toTypedArray())
      )

      is ChannelListViewEvent.OpenThermometerDetail -> navigator.navigateTo(
        R.id.thermostat_detail_fragment,
        ThermometerDetailFragment.bundle(event.itemBundle, event.pages.toTypedArray())
      )

      else -> {}
    }
  }

  override fun handleViewState(state: ChannelListViewState) {
    adapter.setItems(state.channels)

    if (scrollDownOnReload) {
      binding.channelsList.smoothScrollBy(0, 50.toPx())
      scrollDownOnReload = false
    }
  }

  private fun setupAdapter() {
    adapter.leftButtonClickCallback = {
      SuplaApp.Vibrate(context)
      viewModel.performAction(it, ButtonType.LEFT)
    }
    adapter.rightButtonClickCallback = {
      SuplaApp.Vibrate(context)
      viewModel.performAction(it, ButtonType.RIGHT)
    }
    adapter.swappedElementsCallback = { first, second -> viewModel.swapItems(first, second) }
    adapter.reloadCallback = { viewModel.loadChannels() }
    adapter.toggleLocationCallback = { location, scrollDown ->
      viewModel.toggleLocationCollapsed(location)
      scrollDownOnReload = scrollDown
    }
    adapter.infoButtonClickCallback = { statePopup.show(it) }
    adapter.issueButtonClickCallback = { showAlertPopup(it) }
    adapter.listItemClickCallback = { viewModel.onListItemClick(it as Channel) }
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    when (message.type) {
      SuplaClientMsg.onChannelState -> handleChannelState(message.channelState)
      SuplaClientMsg.onDataChanged -> handleChannelChange(message.channelId)
    }
  }

  private fun handleChannelState(state: SuplaChannelState?) {
    if (state != null && statePopup.isVisible && statePopup.remoteId == state.channelID) {
      statePopup.update(state)
    }
  }

  private fun handleChannelChange(channelId: Int) {
    if (statePopup.isVisible && statePopup.remoteId == channelId) {
      statePopup.update(channelId)
    }
  }

  private fun showAlertPopup(messageId: Int?) {
    if (messageId == null) {
      return
    }
    val builder = AlertDialog.Builder(context)
    builder.setTitle(android.R.string.dialog_alert_title)
    builder.setMessage(messageId)
    builder.setNeutralButton(R.string.ok) { dialog, _ -> dialog.cancel() }
    builder.create().show()
  }
}
