package org.supla.android.features.grouplist

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.BaseViewModel

@AndroidEntryPoint
class GroupListFragment : BaseFragment<GroupListViewState, GroupListViewEvent>(R.layout.fragment_group_list) {

  private val viewModel: GroupListViewModel by viewModels()

  override fun getViewModel(): BaseViewModel<GroupListViewState, GroupListViewEvent> = viewModel

  override fun handleEvents(event: GroupListViewEvent) {
  }

  override fun handleViewState(state: GroupListViewState) {
  }
}