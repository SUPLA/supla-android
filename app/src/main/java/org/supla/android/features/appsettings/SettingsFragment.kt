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

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.databinding.FragmentSettingsBinding
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.MainRoute
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragment<SettingsViewState, SettingsViewEvent>(R.layout.fragment_settings) {

  override val viewModel: SettingsViewModel by viewModels()
  private val binding by viewBinding(FragmentSettingsBinding::bind)

  @Inject
  lateinit var navigator: MainComposeNavigator

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
      SettingsViewEvent.NavigateToLocalizationsOrdering -> navigator.navigateTo(MainRoute.LocationReorder)
      SettingsViewEvent.NavigateToAndroidAuto -> navigator.navigateTo(MainRoute.AndroidAutoItems)
      SettingsViewEvent.NavigateToNfc -> navigator.navigateTo(MainRoute.NfcTagList)
      is SettingsViewEvent.NavigateToPinSetup -> navigator.navigateTo(MainRoute.PinSetup(event.lockScreenScope))
      is SettingsViewEvent.NavigateToPinVerification -> navigator.navigateTo(MainRoute.Unlock(event.verificationAction))
      SettingsViewEvent.NavigateToSettings -> navigator.navigateToSystemSettings()
    }
  }

  override fun handleViewState(state: SettingsViewState) {
    adapter.setItems(state.settingsItems)
  }
}
