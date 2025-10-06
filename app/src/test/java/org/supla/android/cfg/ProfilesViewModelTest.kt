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

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.features.lockscreen.UnlockAction
import org.supla.android.features.profileslist.ProfilesListState
import org.supla.android.features.profileslist.ProfilesListViewEvent
import org.supla.android.features.profileslist.ProfilesListViewModel
import org.supla.android.features.profileslist.ProfilesListViewState
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.lock.GetLockScreenSettingUseCase
import org.supla.android.usecases.profile.ActivateProfileUseCase
import org.supla.android.usecases.profile.ReadAllProfilesUseCase

class ProfilesViewModelTest : BaseViewModelTest<ProfilesListState, ProfilesListViewEvent, ProfilesListViewModel>(MockSchedulers.MOCKK) {
  @MockK
  private lateinit var readAllProfilesUseCase: ReadAllProfilesUseCase

  @MockK
  private lateinit var activateProfileUseCase: ActivateProfileUseCase

  @MockK
  private lateinit var getLockScreenSettingUseCase: GetLockScreenSettingUseCase

  @MockK
  private lateinit var profileRepository: RoomProfileRepository

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  @InjectMockKs
  override lateinit var viewModel: ProfilesListViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `should load profiles`() {
    // given
    val profiles = listOf<ProfileEntity>(mockk())
    every { readAllProfilesUseCase.invoke() } returns Observable.just(profiles)

    // when
    viewModel.onViewCreated()

    // then
    assertThat(states).containsExactly(ProfilesListState(ProfilesListViewState(profiles)))
    assertThat(events).isEmpty()

    verify {
      readAllProfilesUseCase.invoke()
    }
    confirmVerified(readAllProfilesUseCase, activateProfileUseCase)
  }

  @Test
  fun `should activate profile`() {
    // given
    val profileId = 123L
    val profile: ProfileEntity = mockk {
      every { id } returns profileId
      every { active } returns false
    }
    every { activateProfileUseCase.invoke(profileId, force = true) } returns Completable.complete()

    // when
    viewModel.onProfileSelected(profile)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(ProfilesListViewEvent.Finish)

    verify {
      activateProfileUseCase.invoke(profileId, force = true)
    }
    confirmVerified(readAllProfilesUseCase, activateProfileUseCase)
  }

  @Test
  fun `should open profile edit view`() {
    // given
    val profileId: Long = 123
    every { getLockScreenSettingUseCase.invoke() } returns LockScreenScope.NONE

    // when
    viewModel.onEditClicked(profileId)

    // then
    assertThat(events).containsExactly(ProfilesListViewEvent.NavigateToProfileEdit(profileId))
  }

  @Test
  fun `should open lock screen when user wants to edit profile but lock scope is set to accounts`() {
    // given
    val profileId: Long = 123
    every { getLockScreenSettingUseCase.invoke() } returns LockScreenScope.ACCOUNTS

    // when
    viewModel.onEditClicked(profileId)

    // then
    assertThat(events).containsExactly(ProfilesListViewEvent.NavigateToLockScreen(UnlockAction.AuthorizeAccountsEdit(profileId)))
  }

  @Test
  fun `should open new profile view`() {
    // given
    every { getLockScreenSettingUseCase.invoke() } returns LockScreenScope.NONE

    // when
    viewModel.onAddAccount()

    // then
    assertThat(events).containsExactly(ProfilesListViewEvent.NavigateToProfileCreate)
  }

  @Test
  fun `should open lock screen when user wants to add profile but lock scope is set to accounts`() {
    // given
    every { getLockScreenSettingUseCase.invoke() } returns LockScreenScope.ACCOUNTS

    // when
    viewModel.onAddAccount()

    // then
    assertThat(events).containsExactly(ProfilesListViewEvent.NavigateToLockScreen(UnlockAction.AuthorizeAccountsCreate))
  }
}
