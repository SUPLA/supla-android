package org.supla.android.features.status
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
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.databinding.FragmentComposeBinding
import org.supla.android.navigator.MainNavigator
import org.supla.android.ui.dialogs.AuthorizationDialog
import javax.inject.Inject

@AndroidEntryPoint
class StatusFragment : BaseFragment<StatusViewModelState, StatusViewEvent>(R.layout.fragment_compose) {
  override val viewModel: StatusViewModel by viewModels()
  private val binding by viewBinding(FragmentComposeBinding::bind)

  @Inject
  lateinit var navigator: MainNavigator

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.composeContent.setContent {
      val modelState by viewModel.getViewState().collectAsState()
      SuplaTheme {
        modelState.authorizationDialogState?.let {
          AuthorizationDialog(
            dialogState = it,
            onCancel = { viewModel.closeAuthorizationDialog() },
            onAuthorize = viewModel::login,
            onStateChange = viewModel::updateAuthorizationState
          )
        }

        when (modelState.viewType) {
          StatusViewModelState.ViewType.CONNECTING ->
            ConnectionStatusView(
              viewState = modelState.viewState,
              onCancelAndGoToProfilesClick = { viewModel.cancelAndOpenProfiles() }
            )

          StatusViewModelState.ViewType.ERROR ->
            ErrorStatusView(
              viewState = modelState.viewState,
              onTryAgainClick = { viewModel.tryAgainClick() },
              onProfilesClick = { viewModel.cancelAndOpenProfiles() }
            )
        }
      }
    }
  }

  override fun getToolbarVisible(): Boolean = false

  override fun onStart() {
    super.onStart()
    viewModel.onStart()
  }

  override fun handleEvents(event: StatusViewEvent) {
    when (event) {
      is StatusViewEvent.NavigateToMain -> navigator.navigateToMain()
      is StatusViewEvent.NavigateToProfiles -> navigator.navigateToProfiles()
    }
  }

  override fun handleViewState(state: StatusViewModelState) {
  }
}
