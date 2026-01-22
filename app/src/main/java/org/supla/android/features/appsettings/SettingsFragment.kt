package org.supla.android.features.appsettings
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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.databinding.FragmentSettingsBinding
import org.supla.android.features.lockscreen.LockScreenFragment
import org.supla.android.features.pinsetup.PinSetupFragment
import org.supla.android.navigator.MainNavigator
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragment<SettingsViewState, SettingsViewEvent>(R.layout.fragment_settings) {

  override val viewModel: SettingsViewModel by viewModels()
  private val binding by viewBinding(FragmentSettingsBinding::bind)

  @Inject
  lateinit var navigator: MainNavigator

  @Inject
  lateinit var adapter: SettingsListAdapter

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.settingsList.adapter = adapter
    val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
    divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.settings_item_decorator)!!)
    binding.settingsList.addItemDecoration(divider)
  }

  override fun onStart() {
    super.onStart()
    viewModel.loadSettings()
  }

  override fun handleEvents(event: SettingsViewEvent) {
    when (event) {
      SettingsViewEvent.NavigateToLocalizationsOrdering ->
        navigator.navigateTo(R.id.location_ordering_fragment)

      SettingsViewEvent.NavigateToAndroidAuto ->
        navigator.navigateTo(R.id.android_auto_items_fragment)

      SettingsViewEvent.NavigateToNfc ->
        navigator.navigateTo(R.id.nfc_tag_list_fragment)

      is SettingsViewEvent.NavigateToPinSetup ->
        navigator.navigateTo(R.id.pin_setup_fragment, PinSetupFragment.bundle(event.lockScreenScope))

      is SettingsViewEvent.NavigateToPinVerification ->
        navigator.navigateTo(R.id.lock_screen_fragment, LockScreenFragment.bundle(event.verificationAction))

      SettingsViewEvent.NavigateToSettings -> {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
          data = Uri.fromParts("package", requireActivity().packageName, null)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          startActivity(this)
        }
      }
    }
  }

  override fun handleViewState(state: SettingsViewState) {
    adapter.setItems(state.settingsItems)
  }
}
