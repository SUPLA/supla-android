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
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.databinding.FragmentGroupListBinding
import org.supla.android.features.legacydetail.LegacyDetailFragment
import org.supla.android.navigator.MainNavigator
import org.supla.android.ui.dialogs.exceededAmperageDialog
import org.supla.android.ui.dialogs.valveAlertDialog
import org.supla.android.usecases.channel.ButtonType
import javax.inject.Inject

@AndroidEntryPoint
class GroupListFragment : BaseFragment<GroupListViewState, GroupListViewEvent>(R.layout.fragment_group_list) {

  private val viewModel: GroupListViewModel by viewModels()
  private val binding by viewBinding(FragmentGroupListBinding::bind)

  @Inject
  lateinit var adapter: GroupsAdapter

  @Inject
  lateinit var suplaClientProvider: SuplaClientProvider

  @Inject
  lateinit var navigator: MainNavigator

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.groupsList.adapter = adapter
    setupAdapter()
  }

  override fun onStart() {
    super.onStart()
    viewModel.loadGroups()
  }

  override fun getViewModel(): BaseViewModel<GroupListViewState, GroupListViewEvent> = viewModel

  override fun handleEvents(event: GroupListViewEvent) {
    val suplaClient = suplaClientProvider.provide()
    when (event) {
      is GroupListViewEvent.ShowValveDialog -> valveAlertDialog(event.remoteId, suplaClient).show()
      is GroupListViewEvent.ShowAmperageExceededDialog -> exceededAmperageDialog(event.remoteId, suplaClient).show()
      is GroupListViewEvent.OpenLegacyDetails -> navigator.navigateToLegacyDetails(
        event.remoteId,
        event.type,
        LegacyDetailFragment.ItemType.GROUP
      )
      is GroupListViewEvent.ReassignAdapter -> {
        binding.groupsList.adapter = null
        binding.groupsList.adapter = adapter
      }
    }
  }

  override fun handleViewState(state: GroupListViewState) {
    adapter.setItems(state.groups)
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
    adapter.swappedElementsCallback = { firstItem, secondItem -> viewModel.swapItems(firstItem, secondItem) }
    adapter.reloadCallback = { viewModel.loadGroups() }
    adapter.toggleLocationCallback = { viewModel.toggleLocationCollapsed(it) }
    adapter.listItemClickCallback = { viewModel.onListItemClick(it) }
  }
}
