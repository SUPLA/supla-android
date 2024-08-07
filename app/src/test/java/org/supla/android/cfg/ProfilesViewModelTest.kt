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

import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.BaseViewModelTest
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.features.lockscreen.UnlockAction
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.lock.GetLockScreenSettingUseCase
import org.supla.android.usecases.profile.ActivateProfileUseCase
import org.supla.android.usecases.profile.ReadAllProfilesUseCase

@RunWith(MockitoJUnitRunner::class)
class ProfilesViewModelTest : BaseViewModelTest<ProfilesViewState, ProfilesViewEvent, ProfilesViewModel>() {
  @Mock
  private lateinit var readAllProfilesUseCase: ReadAllProfilesUseCase

  @Mock
  private lateinit var activateProfileUseCase: ActivateProfileUseCase

  @Mock
  private lateinit var getLockScreenSettingUseCase: GetLockScreenSettingUseCase

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @InjectMocks
  override lateinit var viewModel: ProfilesViewModel

  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun `should load profiles`() {
    // given
    val profiles = listOf<ProfileEntity>(mockk())
    whenever(readAllProfilesUseCase.invoke()).thenReturn(Observable.just(profiles))

    // when
    viewModel.onViewCreated()

    // then
    assertThat(states).containsExactly(ProfilesViewState(profiles))
    assertThat(events).isEmpty()

    verify(readAllProfilesUseCase).invoke()
    verifyNoMoreInteractions(readAllProfilesUseCase)
    verifyNoInteractions(activateProfileUseCase)
  }

  @Test
  fun `should activate profile`() {
    // given
    val profileId = 123L
    whenever(activateProfileUseCase.invoke(profileId, force = true)).thenReturn(Completable.complete())

    // when
    viewModel.activateProfile(profileId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(ProfilesViewEvent.Finish)

    verify(activateProfileUseCase).invoke(profileId, force = true)
    verifyNoMoreInteractions(activateProfileUseCase)
    verifyNoInteractions(readAllProfilesUseCase)
  }

  @Test
  fun `should open profile edit view`() {
    // given
    val profileId: Long = 123
    whenever(getLockScreenSettingUseCase.invoke()).thenReturn(LockScreenScope.NONE)

    // when
    viewModel.onEditProfileClick(profileId)

    // then
    assertThat(events).containsExactly(ProfilesViewEvent.NavigateToProfileEdit(profileId))
  }

  @Test
  fun `should open lock screen when user wants to edit profile but lock scope is set to accounts`() {
    // given
    val profileId: Long = 123
    whenever(getLockScreenSettingUseCase.invoke()).thenReturn(LockScreenScope.ACCOUNTS)

    // when
    viewModel.onEditProfileClick(profileId)

    // then
    assertThat(events).containsExactly(ProfilesViewEvent.NavigateToLockScreen(UnlockAction.AuthorizeAccountsEdit(profileId)))
  }

  @Test
  fun `should open new profile view`() {
    // given
    whenever(getLockScreenSettingUseCase.invoke()).thenReturn(LockScreenScope.NONE)

    // when
    viewModel.onCreateProfileClick()

    // then
    assertThat(events).containsExactly(ProfilesViewEvent.NavigateToProfileCreate)
  }

  @Test
  fun `should open lock screen when user wants to add profile but lock scope is set to accounts`() {
    // given
    whenever(getLockScreenSettingUseCase.invoke()).thenReturn(LockScreenScope.ACCOUNTS)

    // when
    viewModel.onCreateProfileClick()

    // then
    assertThat(events).containsExactly(ProfilesViewEvent.NavigateToLockScreen(UnlockAction.AuthorizeAccountsCreate))
  }
}
