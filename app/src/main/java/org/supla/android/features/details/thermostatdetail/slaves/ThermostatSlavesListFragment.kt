package org.supla.android.features.details.thermostatdetail.slaves
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.captionchangedialog.CaptionChangeViewModel
import org.supla.android.features.captionchangedialog.View
import org.supla.android.features.details.thermostatdetail.ThermostatDetailFragment
import org.supla.android.features.statedialog.StateDialogViewModel
import org.supla.android.features.statedialog.View
import org.supla.android.features.statedialog.handleStateDialogViewEvent
import org.supla.android.navigator.MainNavigator
import org.supla.android.ui.dialogs.AlertDialog
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage
import javax.inject.Inject

@AndroidEntryPoint
class ThermostatSlavesListFragment : BaseComposeFragment<ThermostatSlavesListViewModelState, ThermostatSlavesListViewEvent>() {

  override val viewModel: ThermostatSlavesListViewModel by viewModels()
  override val helperViewModels: List<BaseViewModel<*, *>>
    get() = listOf(stateDialogViewModel, captionChangeViewModel)

  private val stateDialogViewModel: StateDialogViewModel by viewModels()
  private val captionChangeViewModel: CaptionChangeViewModel by viewModels()

  @Inject
  lateinit var navigator: MainNavigator

  @Composable
  override fun ComposableContent(modelState: ThermostatSlavesListViewModelState) {
    SuplaTheme {
      modelState.showMessage?.let {
        AlertDialog(
          title = stringResource(id = android.R.string.dialog_alert_title),
          message = it,
          positiveButtonTitle = stringResource(id = R.string.ok),
          negativeButtonTitle = null,
          onPositiveClick = viewModel::closeMessage
        )
      }
      stateDialogViewModel.View()
      captionChangeViewModel.View()

      viewModel.View(
        state = modelState.viewState,
      )
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.onCreate(item.remoteId)
  }

  override fun onStart() {
    super.onStart()
    viewModel.onStart()
  }

  override fun onStop() {
    super.onStop()
    viewModel.onStop()
  }

  override fun handleEvents(event: ThermostatSlavesListViewEvent) {
    when (event) {
      is ThermostatSlavesListViewEvent.ChangeCaption ->
        captionChangeViewModel.showChannelDialog(event.data.channelId, event.data.profileId, event.data.userCaption)

      is ThermostatSlavesListViewEvent.OpenDetails ->
        navigator.navigateTo(R.id.thermostat_detail_fragment, ThermostatDetailFragment.bundle(event.bundle, event.pages.toTypedArray()))

      is ThermostatSlavesListViewEvent.ShowInfo ->
        stateDialogViewModel.showDialog(event.data.channelId)
    }
  }

  override fun handleHelperEvents(event: ViewEvent) {
    handleStateDialogViewEvent(event)
  }

  override fun onSuplaMessage(message: SuplaClientMessage) {
    (message as? SuplaClientMessage.ChannelState)?.let { stateDialogViewModel.updateStateDialog(it.channelState) }
  }
}
