package org.supla.android.features.grouplist
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
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.databinding.FragmentGroupListBinding
import org.supla.android.extensions.toPx
import org.supla.android.extensions.visibleIf
import org.supla.android.features.captionchangedialog.CaptionChangeViewModel
import org.supla.android.features.captionchangedialog.View
import org.supla.android.navigator.MainNavigator
import org.supla.android.usecases.channel.ButtonType
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage
import javax.inject.Inject

@AndroidEntryPoint
class GroupListFragment : BaseFragment<GroupListViewState, GroupListViewEvent>(R.layout.fragment_group_list) {

  override val viewModel: GroupListViewModel by viewModels()
  override val helperViewModels: List<BaseViewModel<*, *>>
    get() = listOf(captionChangeViewModel)

  private val captionChangeViewModel: CaptionChangeViewModel by viewModels()
  private val binding by viewBinding(FragmentGroupListBinding::bind)
  private var scrollDownOnReload = false

  @Inject
  lateinit var adapter: GroupsAdapter

  @Inject
  lateinit var navigator: MainNavigator

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.groupsList.adapter = adapter
    binding.groupsList.itemAnimator = null
    setupAdapter()
    captionChangeViewModel.finishedCallback = { it.isLocation.ifTrue { viewModel.loadGroups() } }
    binding.groupsEmptyListButton.setOnClickListener { viewModel.onAddGroupClick() }
    binding.composeView.setContent {
      val modelState by viewModel.getViewState().collectAsState()
      SuplaTheme {
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
    viewModel.loadGroups()
  }

  override fun handleEvents(event: GroupListViewEvent) {
    when (event) {
      is GroupListViewEvent.NavigateToPrivateCloud -> navigator.navigateToWeb(event.url)
      is GroupListViewEvent.NavigateToSuplaCloud -> navigator.navigateToCloudExternal()
      is GroupListViewEvent.NavigateToSuplaBetaCloud -> navigator.navigateToBetaCloudExternal()
      is GroupListViewEvent.OpenLegacyDetails -> {
        setToolbarTitle("")
        navigator.navigateToLegacyDetails(
          event.remoteId,
          event.type,
          ItemType.GROUP
        )
      }

      is GroupListViewEvent.ReassignAdapter -> {
        binding.groupsList.adapter = null
        binding.groupsList.adapter = adapter
      }

      is GroupListViewEvent.BaseDetail -> navigator.navigateTo(
        destinationId = event.fragmentId,
        bundle = event.fragmentArguments
      )
    }
  }

  override fun handleViewState(state: GroupListViewState) {
    state.groups?.let { adapter.setItems(it) }

    binding.groupsEmptyListIcon.visibleIf(state.groups?.isEmpty() == true)
    binding.groupsEmptyListLabel.visibleIf(state.groups?.isEmpty() == true)
    binding.groupsEmptyListButton.visibleIf(state.groups?.isEmpty() == true)

    if (scrollDownOnReload) {
      binding.groupsList.smoothScrollBy(0, 50.toPx())
      scrollDownOnReload = false
    }
  }

  override fun onSuplaMessage(message: SuplaClientMessage) {
    (message as? SuplaClientMessage.GroupDataChanged)?.let { viewModel.updateGroup(it.groupId) }
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
    adapter.swappedElementsCallback = { firstItem, secondItem -> viewModel.swapItems(firstItem, secondItem) }
    adapter.toggleLocationCallback = { location, scrollDown ->
      viewModel.toggleLocationCollapsed(location)
      scrollDownOnReload = scrollDown
    }
    adapter.listItemClickCallback = { viewModel.onListItemClick(it) }
    adapter.captionLongPressCallback = captionChangeViewModel::showGroupDialog
    adapter.locationCaptionLongPressCallback = captionChangeViewModel::showLocationDialog
  }
}
