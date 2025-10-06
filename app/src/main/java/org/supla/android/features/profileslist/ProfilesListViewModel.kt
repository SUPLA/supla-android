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

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.features.lockscreen.UnlockAction
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.lock.GetLockScreenSettingUseCase
import org.supla.android.usecases.profile.ActivateProfileUseCase
import org.supla.android.usecases.profile.ReadAllProfilesUseCase
import javax.inject.Inject

@HiltViewModel
class ProfilesListViewModel @Inject constructor(
  private val getLockScreenSettingUseCase: GetLockScreenSettingUseCase,
  private val readAllProfilesUseCase: ReadAllProfilesUseCase,
  private val activateProfileUseCase: ActivateProfileUseCase,
  private val profileRepository: RoomProfileRepository,
  schedulers: SuplaSchedulers
) : BaseViewModel<ProfilesListState, ProfilesListViewEvent>(ProfilesListState(), schedulers), ProfilesListScope {

  override fun onViewCreated() {
    readAllProfilesUseCase()
      .attach()
      .subscribeBy(
        onNext = { profiles ->
          updateState {
            it.copy(
              viewState = it.viewState.copy(
                profiles = profiles
              )
            )
          }
        }
      )
      .disposeBySelf()
  }

  override fun onEditClicked(id: Long) {
    if (getLockScreenSettingUseCase() == LockScreenScope.ACCOUNTS) {
      sendEvent(ProfilesListViewEvent.NavigateToLockScreen(UnlockAction.AuthorizeAccountsEdit(id)))
    } else {
      sendEvent(ProfilesListViewEvent.NavigateToProfileEdit(id))
    }
  }

  override fun onProfileSelected(profile: ProfileEntity) {
    activateProfileUseCase(profile.id!!, force = true)
      .attach()
      .subscribeBy(
        onComplete = { sendEvent(ProfilesListViewEvent.Finish) },
        onError = defaultErrorHandler("activateProfile")
      )
      .disposeBySelf()
  }

  override fun onAddAccount() {
    if (getLockScreenSettingUseCase() == LockScreenScope.ACCOUNTS) {
      sendEvent(ProfilesListViewEvent.NavigateToLockScreen(UnlockAction.AuthorizeAccountsCreate))
    } else {
      sendEvent(ProfilesListViewEvent.NavigateToProfileCreate)
    }
  }

  override fun onMove(from: Int, to: Int) {
    updateState {
      it.copy(
        viewState = it.viewState.copy(
          profiles = it.viewState.profiles.toMutableList().apply {
            add(to, removeAt(from))
          }
        )
      )
    }
  }

  override fun onMoveFinished() {
    profileRepository.setItemsOrder(currentState().viewState.profiles.map { it.id!! })
      .attach()
      .subscribeBy(onError = defaultErrorHandler("onMoveFinished"))
      .disposeBySelf()
  }
}

sealed class ProfilesListViewEvent : ViewEvent {
  data object Finish : ProfilesListViewEvent()
  data class NavigateToLockScreen(val unlockAction: UnlockAction) : ProfilesListViewEvent()
  data class NavigateToProfileEdit(val profileId: Long) : ProfilesListViewEvent()
  data object NavigateToProfileCreate : ProfilesListViewEvent()
}

data class ProfilesListState(
  val viewState: ProfilesListViewState = ProfilesListViewState(),
) : ViewState()
