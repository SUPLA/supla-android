package org.supla.android.features.channellist

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
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.databinding.FragmentChannelListBinding
import org.supla.android.features.legacydetail.LegacyDetailFragment
import org.supla.android.lib.SuplaChannelState
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.navigator.MainNavigator
import org.supla.android.ui.dialogs.exceededAmperageDialog
import org.supla.android.ui.dialogs.valveAlertDialog
import org.supla.android.usecases.channel.ButtonType
import javax.inject.Inject

@AndroidEntryPoint
class ChannelListFragment : BaseFragment<ChannelListViewState, ChannelListViewEvent>(R.layout.fragment_channel_list) {

  private val viewModel: ChannelListViewModel by viewModels()
  private val binding by viewBinding(FragmentChannelListBinding::bind)
  private lateinit var statePopup: ChannelStatePopup

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

  override fun getViewModel(): BaseViewModel<ChannelListViewState, ChannelListViewEvent> = viewModel

  override fun handleEvents(event: ChannelListViewEvent) {
    val suplaClient = suplaClientProvider.provide()
    when (event) {
      is ChannelListViewEvent.ShowValveDialog -> valveAlertDialog(event.remoteId, suplaClient).show()
      is ChannelListViewEvent.ShowAmperageExceededDialog -> exceededAmperageDialog(event.remoteId, suplaClient).show()
      is ChannelListViewEvent.OpenLegacyDetails -> navigator.navigateToLegacyDetails(
        event.remoteId,
        event.type,
        LegacyDetailFragment.ItemType.CHANNEL
      )
      is ChannelListViewEvent.ReassignAdapter -> {
        binding.channelsList.adapter = null
        binding.channelsList.adapter = adapter
      }
    }
  }

  override fun handleViewState(state: ChannelListViewState) {
    if (state.channels != null) {
      adapter.setItems(state.channels)
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
    adapter.toggleLocationCallback = { viewModel.toggleLocationCollapsed(it) }
    adapter.infoButtonClickCallback = { statePopup.show(it) }
    adapter.listItemClickCallback = { viewModel.onListItemClick(it) }
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
}
