package org.supla.android.cfg
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
import org.supla.android.core.ui.BaseFragment
import org.supla.android.databinding.FragmentProfilesBinding
import org.supla.android.features.createaccount.CreateAccountFragment
import org.supla.android.features.lockscreen.LockScreenFragment
import org.supla.android.navigator.CfgActivityNavigator
import org.supla.android.profile.ProfileManager
import javax.inject.Inject

@AndroidEntryPoint
class ProfilesFragment : BaseFragment<ProfilesViewState, ProfilesViewEvent>(R.layout.fragment_profiles) {

  override val viewModel: ProfilesViewModel by viewModels()
  private val binding by viewBinding(FragmentProfilesBinding::bind)

  @Inject
  internal lateinit var profileManager: ProfileManager

  @Inject
  internal lateinit var navigator: CfgActivityNavigator

  @Inject
  internal lateinit var adapter: ProfilesAdapter

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    adapter.onActivateClickListener = { viewModel.activateProfile(it) }
    adapter.onAddClickListener = viewModel::onCreateProfileClick
    adapter.onEditClickListener = viewModel::onEditProfileClick

    binding.profilesList.adapter = adapter
  }

  override fun handleEvents(event: ProfilesViewEvent) {
    when (event) {
      is ProfilesViewEvent.Finish -> navigator.navigateToMain()
      ProfilesViewEvent.NavigateToProfileCreate -> navigator.navigateTo(R.id.cfgNewProfile)
      is ProfilesViewEvent.NavigateToProfileEdit -> navigator.navigateTo(R.id.cfgEditProfile, CreateAccountFragment.bundle(event.profileId))
      is ProfilesViewEvent.NavigateToLockScreen ->
        navigator.navigateTo(R.id.config_lock_screen_fragment, LockScreenFragment.bundle(event.unlockAction))
    }
  }

  override fun handleViewState(state: ProfilesViewState) {
    adapter.setData(state.profiles)
  }
}
