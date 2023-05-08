package org.supla.android.features.channellist

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.BaseViewModel

@AndroidEntryPoint
class ChannelListFragment : BaseFragment<ChannelListViewState, ChannelListViewEvent>(R.layout.fragment_channel_list) {

  private val viewModel: ChannelListViewModel by viewModels()

  override fun getViewModel(): BaseViewModel<ChannelListViewState, ChannelListViewEvent> = viewModel

  override fun handleEvents(event: ChannelListViewEvent) {
  }

  override fun handleViewState(state: ChannelListViewState) {
  }
}