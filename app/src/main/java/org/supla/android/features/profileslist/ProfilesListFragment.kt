package org.supla.android.features.profileslist
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

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.createaccount.CreateAccountFragment
import org.supla.android.features.lockscreen.LockScreenFragment
import org.supla.android.navigator.CfgActivityNavigator
import javax.inject.Inject

@AndroidEntryPoint
class ProfilesListFragment : BaseComposeFragment<ProfilesListState, ProfilesListViewEvent>() {
  override val viewModel: ProfilesListViewModel by viewModels()

  @Inject
  internal lateinit var navigator: CfgActivityNavigator

  @Composable
  override fun ComposableContent(modelState: ProfilesListState) {
    SuplaTheme {
      viewModel.View(modelState.viewState)
    }
  }

  override fun handleViewState(state: ProfilesListState) {
  }

  override fun handleEvents(event: ProfilesListViewEvent) {
    when (event) {
      is ProfilesListViewEvent.Finish -> navigator.navigateToMain()
      ProfilesListViewEvent.NavigateToProfileCreate -> navigator.navigateTo(R.id.cfgNewProfile)
      is ProfilesListViewEvent.NavigateToProfileEdit -> navigator.navigateTo(
        R.id.cfgEditProfile,
        CreateAccountFragment.bundle(event.profileId)
      )
      is ProfilesListViewEvent.NavigateToLockScreen ->
        navigator.navigateTo(R.id.config_lock_screen_fragment, LockScreenFragment.bundle(event.unlockAction))
    }
  }
}
