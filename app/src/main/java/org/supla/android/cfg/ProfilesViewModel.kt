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

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.features.lockscreen.UnlockAction
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.lock.GetLockScreenSettingUseCase
import org.supla.android.usecases.profile.ActivateProfileUseCase
import org.supla.android.usecases.profile.ReadAllProfilesUseCase
import javax.inject.Inject

@HiltViewModel
class ProfilesViewModel @Inject constructor(
  private val readAllProfilesUseCase: ReadAllProfilesUseCase,
  private val activateProfileUseCase: ActivateProfileUseCase,
  private val getLockScreenSettingUseCase: GetLockScreenSettingUseCase,
  schedulers: SuplaSchedulers
) : BaseViewModel<ProfilesViewState, ProfilesViewEvent>(ProfilesViewState(), schedulers) {

  override fun onViewCreated() {
    readAllProfilesUseCase()
      .attach()
      .subscribeBy(
        onNext = { profiles -> updateState { it.copy(profiles = profiles) } }
      )
      .disposeBySelf()
  }

  fun activateProfile(profileId: Long) {
    activateProfileUseCase(profileId, force = true)
      .attach()
      .subscribeBy(
        onComplete = { sendEvent(ProfilesViewEvent.Finish) },
        onError = defaultErrorHandler("activateProfile")
      )
      .disposeBySelf()
  }

  fun onEditProfileClick(profileId: Long) {
    if (getLockScreenSettingUseCase() == LockScreenScope.ACCOUNTS) {
      sendEvent(ProfilesViewEvent.NavigateToLockScreen(UnlockAction.AuthorizeAccountsEdit(profileId)))
    } else {
      sendEvent(ProfilesViewEvent.NavigateToProfileEdit(profileId))
    }
  }

  fun onCreateProfileClick() {
    if (getLockScreenSettingUseCase() == LockScreenScope.ACCOUNTS) {
      sendEvent(ProfilesViewEvent.NavigateToLockScreen(UnlockAction.AuthorizeAccountsCreate))
    } else {
      sendEvent(ProfilesViewEvent.NavigateToProfileCreate)
    }
  }
}

sealed class ProfilesViewEvent : ViewEvent {
  data object Finish : ProfilesViewEvent()
  data class NavigateToLockScreen(val unlockAction: UnlockAction) : ProfilesViewEvent()
  data class NavigateToProfileEdit(val profileId: Long) : ProfilesViewEvent()
  data object NavigateToProfileCreate : ProfilesViewEvent()
}

data class ProfilesViewState(
  val profiles: List<ProfileEntity> = emptyList()
) : ViewState()
