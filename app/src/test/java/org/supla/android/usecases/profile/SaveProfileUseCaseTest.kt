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
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.Encryption
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.testhelpers.extensions.mock
import org.supla.android.testhelpers.extensions.mockWithEmail
import kotlin.random.Random

class SaveProfileUseCaseTest {
  @MockK
  private lateinit var deleteProfileRelatedDataUseCase: DeleteProfileRelatedDataUseCase

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @MockK
  private lateinit var profileIdHolder: ProfileIdHolder

  @MockK
  private lateinit var randomGenerator: Random

  @MockK
  private lateinit var context: Context

  @InjectMockKs
  private lateinit var useCase: SaveProfileUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should create new active profile when there is no other profile`() {
    // given
    val (guid, authKey) = mockAuthorizationData()

    val profileId = 123L
    val defaultName = "default"
    val inputProfile = ProfileEntity.mockWithEmail()
    val insertedProfile = inputProfile.copy(active = true, name = defaultName, guid = guid, authKey = authKey)

    every { profileRepository.findAllProfiles() } returns Observable.just(emptyList())
    every { profileRepository.insert(insertedProfile) } returns Single.just(profileId)
    every { profileIdHolder.profileId = profileId } answers {}
    every { context.getString(R.string.profile_default_name) } returns defaultName

    // when
    val testObserver = useCase(inputProfile).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(SaveProfileUseCase.Result(profileId, true))

    verify {
      profileRepository.findAllProfiles()
      profileRepository.insert(insertedProfile)
      profileIdHolder.profileId = profileId
      context.getString(R.string.profile_default_name)
    }
    verify(exactly = 2) {
      randomGenerator.nextBytes(16)
    }
    confirmVerified(profileRepository, profileIdHolder, deleteProfileRelatedDataUseCase, randomGenerator, context)
  }

  @Test
  fun `should create new active profile when there is no profile with same id`() {
    // given
    val (guid, authKey) = mockAuthorizationData()

    val profileId = 123L
    val inputProfile = ProfileEntity.mockWithEmail(id = profileId, name = "other name")
    val insertedProfile = inputProfile.copy(guid = guid, authKey = authKey)

    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(ProfileEntity.mockWithEmail(id = 1)))
    every { profileRepository.insert(insertedProfile) } returns Single.just(profileId)

    // when
    val testObserver = useCase(inputProfile).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(SaveProfileUseCase.Result(profileId, false))

    verify {
      profileRepository.findAllProfiles()
      profileRepository.insert(insertedProfile)
    }
    verify(exactly = 2) {
      randomGenerator.nextBytes(16)
    }
    confirmVerified(profileRepository, profileIdHolder, deleteProfileRelatedDataUseCase, randomGenerator, context)
  }

  @Test
  fun `should update profile without auth data change`() {
    // given
    val profileId = 123L
    val newName = "default"
    val inputProfile = ProfileEntity.mockWithEmail(id = profileId, active = true)
    val updatedProfile = inputProfile.copy(name = newName)

    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(inputProfile))
    every { profileRepository.update(updatedProfile) } returns Completable.complete()

    // when
    val testObserver = useCase(updatedProfile).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(SaveProfileUseCase.Result(profileId, false))

    verify {
      profileRepository.findAllProfiles()
      profileRepository.update(updatedProfile)
    }
    confirmVerified(profileRepository, profileIdHolder, deleteProfileRelatedDataUseCase, randomGenerator, context)
  }

  @Test
  fun `should update profile with auth data change`() {
    // given
    val profileId = 123L
    val newName = "default"
    val inputProfile = ProfileEntity.mockWithEmail(id = profileId, active = true)
    val updatedProfile = inputProfile.copy(name = newName, email = "another@supla.org")

    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(inputProfile))
    every { profileRepository.update(updatedProfile) } returns Completable.complete()
    every { profileIdHolder.profileId = profileId } answers {}
    every { deleteProfileRelatedDataUseCase.invoke(profileId) } returns Completable.complete()

    // when
    val testObserver = useCase(updatedProfile).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(SaveProfileUseCase.Result(profileId, true))

    verify {
      profileRepository.findAllProfiles()
      profileRepository.update(updatedProfile)
      profileIdHolder.profileId = profileId
      deleteProfileRelatedDataUseCase.invoke(profileId)
    }
    confirmVerified(profileRepository, profileIdHolder, deleteProfileRelatedDataUseCase, randomGenerator, context)
  }

  @Test
  fun `should clear server for email address when changed to server auto detect`() {
    // given
    val profileId = 123L
    val name = "default"
    val inputProfile = ProfileEntity.mock(
      id = profileId,
      name = name,
      emailAuth = true,
      email = "test@supla.org",
      serverForEmail = "supla.org",
      serverAutoDetect = false
    )
    val updatedProfile = inputProfile.copy(serverAutoDetect = true)
    val readyToSaveProfile = updatedProfile.copy(serverForEmail = "")

    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(inputProfile))
    every { profileRepository.update(readyToSaveProfile) } returns Completable.complete()
    every { deleteProfileRelatedDataUseCase.invoke(profileId) } returns Completable.complete()

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
    confirmVerified(profileRepository, profileIdHolder, deleteProfileRelatedDataUseCase, randomGenerator, context)
  }

  @Test
  fun `should throw when name is empty`() {
    // given
    val (guid, authKey) = mockAuthorizationData()

    val profile = ProfileEntity.mockWithEmail(name = "")
    val insertedProfile = profile.copy(guid = guid, authKey = authKey)
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
    confirmVerified(profileRepository, profileIdHolder)
  }

  @Test
  fun `should throw when name is duplicated`() {
    // given
    val (guid, authKey) = mockAuthorizationData()

    val profile = ProfileEntity.mockWithEmail()
    val insertedProfile = profile.copy(guid = guid, authKey = authKey)
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
    confirmVerified(profileRepository, profileIdHolder)
  }

  @Test
  fun `should throw when name is duplicated - trimming`() {
    // given
    val (guid, authKey) = mockAuthorizationData()

    val profile = ProfileEntity.mockWithEmail()
    val insertedProfile = profile.copy(guid = guid, authKey = authKey)
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
    confirmVerified(profileRepository, profileIdHolder)
  }

  @Test
  fun `should throw when auth data is not complete`() {
    // given
    val (guid, authKey) = mockAuthorizationData()

    val profile = ProfileEntity.mockWithEmail(id = 2, email = "")
    val insertedProfile = profile.copy(guid = guid, authKey = authKey)
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
    confirmVerified(profileRepository, profileIdHolder)
  }

  private fun mockAuthorizationData(): Pair<ByteArray, ByteArray> {
    val guid = byteArrayOf(1)
    val authKey = byteArrayOf(2)
    val encryptedGuid = byteArrayOf(3)
    val encryptedAuthKey = byteArrayOf(4)

    val appMock: SuplaApp = mockk()
    mockkStatic(SuplaApp::class)
    every { SuplaApp.getApp() } returns appMock

    val deviceId = "some id"
    mockkObject(Preferences.Companion)
    every { Preferences.getDeviceID(appMock) } returns deviceId

    mockkStatic(Encryption::class)
    every { Encryption.encryptDataWithNullOnException(guid, deviceId) } returns encryptedGuid
    every { Encryption.encryptDataWithNullOnException(authKey, deviceId) } returns encryptedAuthKey

    every { randomGenerator.nextBytes(16) } returnsMany listOf(guid, authKey)

    return Pair(encryptedGuid, encryptedAuthKey)
  }
}
