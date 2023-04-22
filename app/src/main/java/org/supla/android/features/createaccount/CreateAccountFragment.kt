package org.supla.android.features.createaccount
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

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.databinding.FragmentCreateAccountBinding
import org.supla.android.extensions.getLongOrNull
import org.supla.android.extensions.visibleIf
import org.supla.android.features.createaccount.dialogs.RemoveConfirmationDialogFragment
import org.supla.android.navigator.CfgActivityNavigator
import javax.inject.Inject

private const val ARG_PROFILE_ID = "arg_profile_id"

@AndroidEntryPoint
class CreateAccountFragment : BaseFragment<CreateAccountViewState, CreateAccountViewEvent>(R.layout.fragment_create_account) {

  @Inject
  internal lateinit var navigator: CfgActivityNavigator

  private val viewModel: CreateAccountViewModel by viewModels()
  override fun getViewModel(): BaseViewModel<CreateAccountViewState, CreateAccountViewEvent> = viewModel

  private val profileId: Long? by lazy { arguments?.getLongOrNull(ARG_PROFILE_ID) }

  private val binding by viewBinding(FragmentCreateAccountBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.loadProfile(profileId)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.lifecycleOwner = this
    binding.viewModel = viewModel
    binding.cfgAdvanced.viewModel = viewModel
    binding.cfgBasic.viewModel = viewModel

    lifecycleScope.launchWhenStarted { viewModel.getViewEvents().collect { event -> handleEvents(event) } }
    lifecycleScope.launchWhenStarted { viewModel.getViewState().collect { state -> handleViewState(state) } }

    setupView()
  }

  override fun handleEvents(event: CreateAccountViewEvent) {
    when (event) {
      is CreateAccountViewEvent.ConfirmDelete -> showConfirmAction()
      is CreateAccountViewEvent.NavigateToCreateAccount -> navigator.navigateToCreateAccount()
      CreateAccountViewEvent.ShowBasicModeUnavailableDialog ->
        showAlertDialog(R.string.basic_profile_warning, R.string.basic_config_unavailable)
      CreateAccountViewEvent.ShowDuplicatedNameDialog ->
        showAlertDialog(R.string.form_error, R.string.form_profile_duplicate)
      CreateAccountViewEvent.ShowEmptyNameDialog ->
        showAlertDialog(R.string.form_error, R.string.form_profile_name_missing)
      CreateAccountViewEvent.ShowRemovalFailureDialog ->
        showAlertDialog(R.string.form_error, R.string.form_profile_removal_failure)
      CreateAccountViewEvent.ShowRequiredDataMissingDialog ->
        showAlertDialog(R.string.form_error, R.string.form_profile_required_data_missing)
      CreateAccountViewEvent.ShowUnknownErrorDialog ->
        showAlertDialog(R.string.form_error, R.string.general_failure)
      CreateAccountViewEvent.Close -> navigator.back()
      CreateAccountViewEvent.RestartFlow -> navigator.restartAppStack()
      CreateAccountViewEvent.Reconnect -> navigator.navigateToStatus()
      is CreateAccountViewEvent.NavigateToWebRemoval ->
        navigator.navigateToDeleteAccount(event.serverAddress, event.destination)
    }
  }

  override fun handleViewState(state: CreateAccountViewState) {
    binding.apply {
      cfgBasic.profileNameLabel.visibleIf(state.profileNameVisible)
      cfgBasic.cfgProfileName.visibleIf(state.profileNameVisible)
      cfgAdvanced.profileNameLabel.visibleIf(state.profileNameVisible)
      cfgAdvanced.cfgProfileName.visibleIf(state.profileNameVisible)

      cfgCbAdvanced.isChecked = state.advancedMode
      cfgDelete.visibleIf(state.deleteButtonVisible)

      cfgAdvanced.authType.position = if (state.authorizeByEmail) 0 else 1
      cfgAdvanced.edServerAddrAutoEmail.isChecked = state.autoServerAddress
      cfgAdvanced.edServerAddrEmail.isEnabled = !state.autoServerAddress

      cfgCreateAccount.visibleIf(state.profileNameVisible.not())
      dontHaveAccountText.visibleIf(state.profileNameVisible.not())
    }
  }

  private fun showAlertDialog(@StringRes title: Int, @StringRes message: Int) {
    AlertDialog.Builder(requireContext())
      .setTitle(title)
      .setMessage(message)
      .setPositiveButton(android.R.string.ok) { dlg, _ ->
        dlg.cancel()
      }
      .create()
      .show()
  }

  private fun showConfirmAction() {
    RemoveConfirmationDialogFragment.create(profileId!!).show(childFragmentManager, null)
  }

  private fun setupView() {
    binding.apply {
      cfgCbAdvanced.setOnCheckedChangeListener { _, advancedMode -> viewModel?.changeMode(advancedMode) }

      cfgBasic.cfgEmail.doOnTextChanged { text, _, _, _ -> viewModel?.changeEmail(text.toString()) }
      cfgAdvanced.cfgEmail.doOnTextChanged { text, _, _, _ -> viewModel?.changeEmail(text.toString()) }

      cfgBasic.cfgProfileName.doOnTextChanged { text, _, _, _ -> viewModel?.changeProfileName(text.toString()) }
      cfgAdvanced.cfgProfileName.doOnTextChanged { text, _, _, _ -> viewModel?.changeProfileName(text.toString()) }

      cfgAdvanced.authType.setOnPositionChangedListener { viewModel?.changeAuthorizeByEmail(it == 0) }
      cfgAdvanced.edServerAddrEmail.doOnTextChanged { text, _, _, _ -> viewModel?.changeEmailAddressServer(text.toString()) }
      cfgAdvanced.edAccessID.doOnTextChanged { text, _, _, _ -> viewModel?.changeAccessIdentifier(text.toString()) }
      cfgAdvanced.edAccessIDpwd.doOnTextChanged { text, _, _, _ -> viewModel?.changeAccessIdentifierPassword(text.toString()) }
      cfgAdvanced.edServerAddr.doOnTextChanged { text, _, _, _ -> viewModel?.changeAccessIdentifierServer(text.toString()) }

      cfgSave.setOnClickListener { viewModel?.saveProfile(profileId, getString(R.string.profile_default_name)) }
      cfgDelete.setOnClickListener { viewModel?.onDeleteProfile() }
    }
  }

  companion object {
    fun bundle(profileId: Long?): Bundle {
      return if (profileId == null) {
        Bundle()
      } else {
        bundleOf(ARG_PROFILE_ID to profileId)
      }
    }
  }
}
