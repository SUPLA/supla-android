package org.supla.android.features.pinsetup
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
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.databinding.FragmentComposeBinding
import org.supla.android.navigator.MainNavigator
import javax.inject.Inject

private const val ARG_LOCK_SCREEN_SETTINGS = "ARG_LOCK_SCREEN_SETTINGS"

@AndroidEntryPoint
class PinSetupFragment : BaseFragment<PinSetupViewModelState, PinSetupViewEvent>(R.layout.fragment_compose) {
  override val viewModel: PinSetupViewModel by viewModels()
  private val binding by viewBinding(FragmentComposeBinding::bind)

  private val lockScreenScope: LockScreenScope by lazy { requireSerializable(ARG_LOCK_SCREEN_SETTINGS, LockScreenScope::class.java) }

  @Inject
  internal lateinit var navigator: MainNavigator

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.composeContent.setContent {
      val modelState by viewModel.getViewState().collectAsState()
      SuplaTheme {
        PinSetupView(
          viewState = modelState.viewState,
          onPinChange = viewModel::onPinChange,
          onSecondPinChange = viewModel::onSecondPinChange,
          onSaveClick = { viewModel.onSaveClick(lockScreenScope) },
          onBiometricAuthenticationChange = viewModel::onBiometricAuthenticationChange
        )
      }
    }
  }

  override fun handleEvents(event: PinSetupViewEvent) {
    when (event) {
      PinSetupViewEvent.Close -> navigator.back()
    }
  }

  override fun handleViewState(state: PinSetupViewModelState) {
  }

  companion object {
    fun bundle(lockScreenScope: LockScreenScope) = bundleOf(
      ARG_LOCK_SCREEN_SETTINGS to lockScreenScope
    )
  }
}
