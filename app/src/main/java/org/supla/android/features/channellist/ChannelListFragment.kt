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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.LightsourceLifespanSettingsDialog
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.databinding.FragmentChannelListBinding
import org.supla.android.extensions.toPx
import org.supla.android.extensions.visibleIf
import org.supla.android.features.statedialog.StateDialog
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.navigator.MainNavigator
import org.supla.android.ui.dialogs.AuthorizationDialog
import org.supla.android.ui.dialogs.exceededAmperageDialog
import org.supla.android.ui.dialogs.valveClosedManuallyDialog
import org.supla.android.ui.dialogs.valveFloodingDialog
import org.supla.android.ui.dialogs.valveMotorProblemDialog
import org.supla.android.ui.lists.message
import org.supla.android.usecases.channel.ButtonType
import javax.inject.Inject

@AndroidEntryPoint
class ChannelListFragment : BaseFragment<ChannelListViewState, ChannelListViewEvent>(R.layout.fragment_channel_list) {

  override val viewModel: ChannelListViewModel by viewModels()
  private val binding by viewBinding(FragmentChannelListBinding::bind)
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
    binding.channelsList.itemAnimator = null
    setupAdapter()
    binding.channelsEmptyListButton.setOnClickListener {
      navigator.navigateToAddWizard()
    }

    binding.yourComposeView.setContent {
      val modelState by viewModel.getViewState().collectAsState()
      SuplaTheme {
        modelState.stateDialogViewState?.let {
          viewModel.StateDialog(state = it)
        }
        modelState.authorizationDialogState?.let {
          viewModel.AuthorizationDialog(state = it)
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()
    viewModel.loadChannels()
    viewModel.onStart()
  }

  override fun onStop() {
    super.onStop()
    viewModel.onStop()
  }

  override fun handleEvents(event: ChannelListViewEvent) {
    val suplaClient = suplaClientProvider.provide()
    when (event) {
      is ChannelListViewEvent.ShowValveClosedManuallyDialog -> valveClosedManuallyDialog(event.remoteId, suplaClient).show()
      is ChannelListViewEvent.ShowValveFloodingDialog -> valveFloodingDialog(event.remoteId, suplaClient).show()
      is ChannelListViewEvent.ShowValveMotorProblemDialog -> valveMotorProblemDialog(event.remoteId, event.action, suplaClient).show()
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

      is ChannelListViewEvent.OpenStandardDetail -> navigator.navigateTo(
        destinationId = event.fragmentId,
        bundle = event.fragmentArguments
      )

      is ChannelListViewEvent.ShowLifespanSettingsDialog -> {
        LightsourceLifespanSettingsDialog(
          context,
          event.remoteId,
          event.lightSourceLifespan ?: 0,
          event.caption(requireContext())
        ).show()
      }
    }
  }

  override fun handleViewState(state: ChannelListViewState) {
    state.channels?.let { adapter.setItems(it) }

    binding.channelsEmptyListIcon.visibleIf(state.channels?.isEmpty() == true)
    binding.channelsEmptyListLabel.visibleIf(state.channels?.isEmpty() == true)
    binding.channelsEmptyListButton.visibleIf(state.channels?.isEmpty() == true)

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
    adapter.infoButtonClickCallback = { viewModel.showStateDialog(it) }
    adapter.issueButtonClickCallback = { showAlertPopup(it.message(requireContext())) }
    adapter.listItemClickCallback = { viewModel.onListItemClick(it) }
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    when (message.type) {
      SuplaClientMsg.onChannelState -> viewModel.updateStateDialog(message.channelState)
    }
  }

  private fun showAlertPopup(messageId: String) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(android.R.string.dialog_alert_title)
    builder.setMessage(messageId)
    builder.setNeutralButton(R.string.ok) { dialog, _ -> dialog.cancel() }
    builder.create().show()
  }
}
