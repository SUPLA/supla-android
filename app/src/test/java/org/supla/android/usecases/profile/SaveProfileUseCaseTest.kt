package org.supla.android.usecases.profile
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

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.supla.android.R
import org.supla.android.core.MainDispatcherRule
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.model.settings.ProfileCredentials
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.di.CoroutineDispatchers
import org.supla.android.testhelpers.extensions.mock
import org.supla.android.testhelpers.extensions.mockWithEmail

@OptIn(ExperimentalCoroutinesApi::class)
class SaveProfileUseCaseTest {

  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @MockK
  private lateinit var deleteProfileRelatedDataUseCase: DeleteProfileRelatedDataUseCase

  @MockK
  private lateinit var encryptedPreferences: EncryptedPreferences

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @MockK
  private lateinit var context: Context

  @MockK
  private lateinit var dispatchers: CoroutineDispatchers

  @InjectMockKs
  private lateinit var useCase: SaveProfileUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    every { dispatchers.io() } returns UnconfinedTestDispatcher()
  }

  @Test
  fun `should create new active profile when there is no other profile`() {
    // given
    val profileId = 123L
    val defaultName = "default"
    val inputProfile = ProfileDto.mockWithEmail()
    val insertedProfile = inputProfile.entity.copy(active = true, name = defaultName)

    every { profileRepository.findAllProfiles() } returns Observable.just(emptyList())
    every { profileRepository.insert(insertedProfile) } returns Single.just(profileId)
    every { context.getString(R.string.profile_default_name) } returns defaultName
    coEvery { encryptedPreferences.getProfileCredentials(profileId) } returns mockk { every { accessIdPassword } returns "" }

    // when
    val testObserver = useCase.invoke(inputProfile).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(SaveProfileUseCase.Result(profileId, true))

    verify {
      profileRepository.findAllProfiles()
      profileRepository.insert(insertedProfile)
      context.getString(R.string.profile_default_name)
    }
    coVerify { encryptedPreferences.getProfileCredentials(profileId) }
    confirmVerified(profileRepository, deleteProfileRelatedDataUseCase, context, encryptedPreferences)
  }

  @Test
  fun `should create new active profile when there is no profile with same id`() {
    // given
    val profileId = 123L
    val inputProfile = ProfileDto.mockWithEmail(id = profileId, name = "other name")
    val insertedProfile = inputProfile.entity.copy()

    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(ProfileEntity.mockWithEmail(id = 1)))
    every { profileRepository.insert(insertedProfile) } returns Single.just(profileId)
    coEvery { encryptedPreferences.getProfileCredentials(profileId) } returns mockk { every { accessIdPassword } returns "" }

    // when
    val testObserver = useCase(inputProfile).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(SaveProfileUseCase.Result(profileId, false))

    verify {
      profileRepository.findAllProfiles()
      profileRepository.insert(insertedProfile)
    }
    coVerify { encryptedPreferences.getProfileCredentials(profileId) }
    confirmVerified(profileRepository, deleteProfileRelatedDataUseCase, context, encryptedPreferences)
  }

  @Test
  fun `should update profile without auth data change`() {
    // given
    val profileId = 123L
    val newName = "default"
    val profile = ProfileDto.mockWithEmail(id = profileId, active = true)
    val inputProfile = profile.copy(name = newName)
    val updatedProfile = inputProfile.entity.copy(name = newName)

    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(inputProfile.entity))
    every { profileRepository.update(updatedProfile) } returns Completable.complete()
    coEvery { encryptedPreferences.getProfileCredentials(profileId) } returns mockk { every { accessIdPassword } returns "" }

    // when
    val testObserver = useCase(inputProfile).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(SaveProfileUseCase.Result(profileId, false))

    verify {
      profileRepository.findAllProfiles()
      profileRepository.update(updatedProfile)
    }
    confirmVerified(profileRepository, deleteProfileRelatedDataUseCase, context)
  }

  @Test
  fun `should update profile with auth data change`() {
    // given
    val profileId = 123L
    val newName = "default"
    val profile = ProfileDto.mockWithEmail(id = profileId, active = true)
    val inputProfile = profile.copy(name = newName, email = "another@supla.org")
    val updatedProfile = inputProfile.entity.copy(name = newName)

    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(profile.entity))
    every { profileRepository.update(updatedProfile) } returns Completable.complete()
    every { deleteProfileRelatedDataUseCase.invoke(profileId) } returns Completable.complete()
    coEvery { encryptedPreferences.getProfileCredentials(profileId) } returns mockk { every { accessIdPassword } returns "" }

    // when
    val testObserver = useCase.invoke(inputProfile).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(SaveProfileUseCase.Result(profileId, true))

    verify {
      profileRepository.findAllProfiles()
      profileRepository.update(updatedProfile)
      deleteProfileRelatedDataUseCase.invoke(profileId)
    }
    confirmVerified(profileRepository, deleteProfileRelatedDataUseCase, context)
  }

  @Test
  fun `should update profile with access id password change`() {
    // given
    val profileId = 123L
    val accessIdPassword = "*****"
    val profile = ProfileDto.mock(id = profileId, active = true)
    val inputProfile = profile.copy(emailAuth = false, accessId = 123, accessIdPassword = accessIdPassword)
    val updatedProfile = inputProfile.entity
    val credentials = ProfileCredentials("", byteArrayOf(), byteArrayOf())
    val updatedCredentials = credentials.copy(accessIdPassword = accessIdPassword)

    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(profile.entity))
    every { profileRepository.update(updatedProfile) } returns Completable.complete()
    every { deleteProfileRelatedDataUseCase.invoke(profileId) } returns Completable.complete()
    coEvery { encryptedPreferences.getProfileCredentials(profileId) } returns credentials
    coEvery { encryptedPreferences.setProfileCredentials(profileId, updatedCredentials) } just Runs

    // when
    val testObserver = useCase.invoke(inputProfile).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(SaveProfileUseCase.Result(profileId, true))

    verify {
      profileRepository.findAllProfiles()
      profileRepository.update(updatedProfile)
      deleteProfileRelatedDataUseCase.invoke(profileId)
    }
    coVerify {
      encryptedPreferences.getProfileCredentials(profileId)
      encryptedPreferences.setProfileCredentials(profileId, updatedCredentials)
    }
    confirmVerified(profileRepository, deleteProfileRelatedDataUseCase, context)
  }

  @Test
  fun `should clear server for email address when changed to server auto detect`() {
    // given
    val profileId = 123L
    val name = "default"
    val profile = ProfileDto.mock(
      id = profileId,
      name = name,
      emailAuth = true,
      email = "test@supla.org",
      serverForEmail = "supla.org",
      serverAutoDetect = false,
      accessIdPassword = ""
    )
    val updatedProfile = profile.copy(serverAutoDetect = true)
    val readyToSaveProfile = updatedProfile.entity.copy(serverForEmail = "")

    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(profile.entity))
    every { profileRepository.update(readyToSaveProfile) } returns Completable.complete()
    every { deleteProfileRelatedDataUseCase.invoke(profileId) } returns Completable.complete()
    coEvery { encryptedPreferences.getProfileCredentials(profileId) } returns mockk { every { accessIdPassword } returns "" }

    // when
    val testObserver = useCase(updatedProfile).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(SaveProfileUseCase.Result(profileId, false))

    verify {
      profileRepository.findAllProfiles()
      profileRepository.update(readyToSaveProfile)
      deleteProfileRelatedDataUseCase.invoke(profileId)
    }
    confirmVerified(profileRepository, deleteProfileRelatedDataUseCase, context)
  }

  @Test
  fun `should throw when name is empty`() {
    // given
    val profile = ProfileDto.mockWithEmail(name = "")
    val insertedProfile = profile.entity
    var insertPerformed = false

    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(ProfileEntity.mockWithEmail()))
    every { profileRepository.insert(insertedProfile) } returns
      Single.fromCallable {
        insertPerformed = true
        1
      }

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertError { it is SaveProfileUseCase.SaveAccountException.EmptyName }

    assertThat(insertPerformed).isFalse
    verify {
      profileRepository.findAllProfiles()
      profileRepository.insert(insertedProfile)
    }
    confirmVerified(profileRepository)
  }

  @Test
  fun `should throw when name is duplicated`() {
    // given
    val profile = ProfileDto.mockWithEmail()
    val insertedProfile = profile.entity
    var insertPerformed = false

    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(ProfileEntity.mockWithEmail(id = 123L)))
    every { profileRepository.insert(insertedProfile) } returns
      Single.fromCallable {
        insertPerformed = true
        1
      }

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertError { it is SaveProfileUseCase.SaveAccountException.DuplicatedName }

    assertThat(insertPerformed).isFalse
    verify {
      profileRepository.findAllProfiles()
      profileRepository.insert(insertedProfile)
    }
    confirmVerified(profileRepository)
  }

  @Test
  fun `should throw when name is duplicated - trimming`() {
    // given
    val profile = ProfileDto.mockWithEmail()
    val insertedProfile = profile.entity
    var insertPerformed = false

    every { profileRepository.findAllProfiles() } returns
      Observable.just(listOf(ProfileEntity.mockWithEmail(id = 123L, name = "test name ")))
    every { profileRepository.insert(insertedProfile) } returns
      Single.fromCallable {
        insertPerformed = true
        2
      }

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertError { it is SaveProfileUseCase.SaveAccountException.DuplicatedName }

    assertThat(insertPerformed).isFalse
    verify {
      profileRepository.findAllProfiles()
      profileRepository.insert(insertedProfile)
    }
    confirmVerified(profileRepository)
  }

  @Test
  fun `should throw when auth data is not complete`() {
    // given
    val profile = ProfileDto.mockWithEmail(id = 2, email = "")
    val insertedProfile = profile.entity
    var insertPerformed = false

    every { profileRepository.findAllProfiles() } returns
      Observable.just(listOf(ProfileEntity.mockWithEmail(id = 1, name = "default")))
    every { profileRepository.insert(insertedProfile) } returns
      Single.fromCallable {
        insertPerformed = true
        2
      }

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertError { it is SaveProfileUseCase.SaveAccountException.DataIncomplete }

    assertThat(insertPerformed).isFalse
    verify {
      profileRepository.findAllProfiles()
      profileRepository.insert(insertedProfile)
    }
    confirmVerified(profileRepository)
  }
}
