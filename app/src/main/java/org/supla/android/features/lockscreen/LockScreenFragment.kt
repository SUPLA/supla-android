package org.supla.android.features.lockscreen
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
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.infrastructure.BiometricUtils
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.databinding.FragmentComposeBinding
import org.supla.android.features.createaccount.CreateAccountFragment
import org.supla.android.navigator.CfgActivityNavigator
import org.supla.android.navigator.MainNavigator
import org.supla.android.ui.dialogs.AlertDialog
import org.supla.android.ui.views.LoadingScrim
import javax.inject.Inject

private const val ARG_LOCK_SCREEN_ACTION = "ARG_LOCK_SCREEN_ACTION"

@AndroidEntryPoint
class LockScreenFragment : BaseFragment<LockScreenViewModelState, LockScreenViewEvent>(R.layout.fragment_compose) {
  override val viewModel: LockScreenViewModel by viewModels()
  private val binding by viewBinding(FragmentComposeBinding::bind)

  private val unlockAction: UnlockAction by lazy { requireSerializable(ARG_LOCK_SCREEN_ACTION, UnlockAction::class.java) }

  @Inject
  internal lateinit var mainNavigator: MainNavigator

  @Inject
  internal lateinit var configNavigator: CfgActivityNavigator

  @Inject
  lateinit var biometricUtils: BiometricUtils

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.onCreate(unlockAction)

    activity?.onBackPressedDispatcher?.addCallback(
      this,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          when (unlockAction) {
            UnlockAction.AuthorizeApplication -> activity?.finishAffinity()
            UnlockAction.TurnOffPin,
            UnlockAction.ConfirmAuthorizeAccounts,
            UnlockAction.ConfirmAuthorizeApplication,
            UnlockAction.AuthorizeAccountsCreate,
            is UnlockAction.AuthorizeAccountsEdit -> configNavigator.back()
          }
        }
      }
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.composeContent.setContent {
      val modelState by viewModel.getViewState().collectAsState()
      SuplaTheme {
        if (modelState.showForgottenCodeDialog) {
          AlertDialog(
            title = stringResource(id = R.string.lock_screen_forgotten_code_title),
            message = stringResource(id = R.string.lock_screen_forgotten_code_message),
            positiveButtonTitle = stringResource(id = R.string.lock_screen_forgotten_code_button),
            negativeButtonTitle = null,
            onPositiveClick = viewModel::hideForgottenCodeDialog,
            onDismiss = viewModel::hideForgottenCodeDialog
          )
        }
        LockScreenView(
          viewState = modelState.viewState,
          onPinChange = viewModel::onPinChange,
          onForgottenCodeClick = viewModel::onForgottenCodeButtonClick,
          onFingerprintIconClick = { showBiometricPrompt() }
        )

        if (modelState.loading) {
          LoadingScrim()
        }
      }
    }
  }

  override fun getToolbarVisible(): Boolean = unlockAction.showToolbar

  override fun handleEvents(event: LockScreenViewEvent) {
    when (event) {
      LockScreenViewEvent.Close -> handleCloseEvent()
      LockScreenViewEvent.ShowBiometricPrompt -> showBiometricPrompt()
    }
  }

  override fun handleViewState(state: LockScreenViewModelState) {
  }

  private fun handleCloseEvent() {
    when (val action = unlockAction) {
      UnlockAction.AuthorizeApplication,
      UnlockAction.TurnOffPin,
      UnlockAction.ConfirmAuthorizeAccounts,
      UnlockAction.ConfirmAuthorizeApplication -> configNavigator.back()

      UnlockAction.AuthorizeAccountsCreate -> {
        configNavigator.back()
        configNavigator.navigateTo(R.id.cfgNewProfile)
      }

      is UnlockAction.AuthorizeAccountsEdit -> {
        configNavigator.back()
        configNavigator.navigateTo(R.id.cfgEditProfile, CreateAccountFragment.bundle(action.profileId))
      }
    }
  }

  private fun showBiometricPrompt() {
    biometricUtils.showBiometricPrompt(
      fragment = this,
      onAuthenticated = viewModel::onBiometricSuccess,
      onAuthenticationFailed = viewModel::onBiometricFailure,
      onError = viewModel::onBiometricError
    )
  }

  companion object {
    fun bundle(unlockAction: UnlockAction) = bundleOf(
      ARG_LOCK_SCREEN_ACTION to unlockAction
    )
  }
}
