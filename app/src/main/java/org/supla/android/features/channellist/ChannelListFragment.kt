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

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.databinding.FragmentChannelListBinding
import org.supla.android.extensions.toPx
import org.supla.android.extensions.visibleIf
import org.supla.android.features.captionchangedialog.CaptionChangeViewModel
import org.supla.android.features.captionchangedialog.View
import org.supla.android.features.statedialog.StateDialogViewModel
import org.supla.android.features.statedialog.View
import org.supla.android.features.statedialog.handleStateDialogViewEvent
import org.supla.android.navigator.MainNavigator
import org.supla.android.ui.lists.message
import org.supla.android.usecases.channel.ButtonType
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage
import javax.inject.Inject

@AndroidEntryPoint
class ChannelListFragment : BaseFragment<ChannelListViewState, ChannelListViewEvent>(R.layout.fragment_channel_list) {

  override val viewModel: ChannelListViewModel by viewModels()
  override val helperViewModels: List<BaseViewModel<*, *>>
    get() = listOf(stateDialogViewModel, captionChangeViewModel)

  private val stateDialogViewModel: StateDialogViewModel by viewModels()
  private val captionChangeViewModel: CaptionChangeViewModel by viewModels()
  private val binding by viewBinding(FragmentChannelListBinding::bind)
  private var scrollDownOnReload = false

  @Inject
  lateinit var adapter: ChannelsAdapter

  @Inject
  lateinit var navigator: MainNavigator

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.channelsList.adapter = adapter
    binding.channelsList.itemAnimator = null
    setupAdapter()
    captionChangeViewModel.finishedCallback = { it.isLocation.ifTrue { viewModel.loadChannels() } }
    binding.channelsEmptyListButton.setOnClickListener {
      navigator.navigateToAddWizard()
    }
    binding.channelsEmptyListDevicesButton.setOnClickListener {
      navigator.navigateTo(R.id.device_catalog_fragment)
    }

    binding.composeView.setContent {
      val modelState by viewModel.getViewState().collectAsState()
      SuplaTheme {
        stateDialogViewModel.View()
        captionChangeViewModel.View()
        modelState.actionAlertDialogState?.View(
          onPositiveClick = { remoteId, actionId -> viewModel.forceAction(remoteId, actionId) },
          onNegativeClick = viewModel::dismissActionDialog
        )
      }
    }
  }

  override fun onStart() {
    super.onStart()
    viewModel.loadChannels()
  }

  override fun handleEvents(event: ChannelListViewEvent) {
    when (event) {
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

      is ChannelListViewEvent.BaseDetail -> navigator.navigateTo(
        destinationId = event.fragmentId,
        bundle = event.fragmentArguments
      )
    }
  }

  override fun handleHelperEvents(event: ViewEvent) {
    handleStateDialogViewEvent(event)
  }

  override fun handleViewState(state: ChannelListViewState) {
    state.channels?.let { adapter.setItems(it) }

    val empty = state.channels?.isEmpty() == true
    binding.channelsEmptyListIcon.visibleIf(empty)
    binding.channelsEmptyListLabel.visibleIf(empty)
    binding.channelsEmptyListButton.visibleIf(empty)
    binding.channelsEmptyListDevicesButton.visibleIf(empty)

    if (scrollDownOnReload) {
      binding.channelsList.smoothScrollBy(0, 50.toPx())
      scrollDownOnReload = false
    }
  }

  private fun setupAdapter() {
    adapter.leftButtonClickCallback = {
      vibrationHelper.vibrate()
      viewModel.performAction(it, ButtonType.LEFT)
    }
    adapter.rightButtonClickCallback = {
      vibrationHelper.vibrate()
      viewModel.performAction(it, ButtonType.RIGHT)
    }
    adapter.swappedElementsCallback = { first, second -> viewModel.swapItems(first, second) }
    adapter.toggleLocationCallback = { location, scrollDown ->
      viewModel.toggleLocationCollapsed(location)
      scrollDownOnReload = scrollDown
    }
    adapter.infoButtonClickCallback = { stateDialogViewModel.showDialog(it) }
    adapter.issueButtonClickCallback = { viewModel.showAlert(it.message(requireContext())) }
    adapter.listItemClickCallback = { viewModel.onListItemClick(it) }
    adapter.captionLongPressCallback = captionChangeViewModel::showChannelDialog
    adapter.locationCaptionLongPressCallback = captionChangeViewModel::showLocationDialog
  }

  override fun onSuplaMessage(message: SuplaClientMessage) {
    (message as? SuplaClientMessage.ChannelState)?.let { stateDialogViewModel.updateStateDialog(it.channelState) }
  }
}
