package org.supla.android.profile

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.ProfileRepository
import org.supla.android.db.AuthProfileItem

@RunWith(MockitoJUnitRunner::class)
class MultiAccountProfileManagerTest {

  @Mock
  private lateinit var profileRepository: ProfileRepository

  private lateinit var profileManager: MultiAccountProfileManager

  @Before
  fun setUp() {
    profileManager = MultiAccountProfileManager(profileRepository)
  }

  @Test
  fun `should create profile`() {
    // given
    val profileId = 123L
    val profile = mockk<AuthProfileItem>()
    every { profile.id } returns null
    every { profile.id = 123L } answers { }

    whenever(profileRepository.createProfile(profile)).thenReturn(profileId)

    // when
    val testObserver = profileManager.create(profile).test()

    // then
    testObserver.assertComplete()

    verify(profileRepository).createProfile(profile)
    verifyNoMoreInteractions(profileRepository)

    io.mockk.verify {
      profile.id
      profile.id = profileId
    }
    confirmVerified(profile)
  }

  @Test
  fun `should fail when trying to create profile with id`() {
    // given
    val profileId = 123L
    val profile = mockk<AuthProfileItem>()
    every { profile.id } returns profileId

    // when
    val testObserver = profileManager.create(profile).test()

    // then
    testObserver.assertFailure(IllegalArgumentException::class.java)

    verifyNoInteractions(profileRepository)

    io.mockk.verify {
      profile.id
    }
    confirmVerified(profile)
  }

  @Test
  fun `should read profile from repository`() {
    // given
    val profileId = 123L
    val profile = mockk<AuthProfileItem>()
    whenever(profileRepository.getProfile(profileId)).thenReturn(profile)

    // when
    val testObserver = profileManager.read(profileId).test()

    // then
    testObserver.assertValue(profile)

    verify(profileRepository).getProfile(profileId)
    verifyNoMoreInteractions(profileRepository)
  }

  @Test
  fun `should update profile`() {
    // given
    val profileId = 123L
    val profile = mockk<AuthProfileItem>()
    every { profile.id } returns profileId

    // when
    val testObserver = profileManager.update(profile).test()

    // then
    testObserver.assertComplete()

    verify(profileRepository).updateProfile(profile)
    verifyNoMoreInteractions(profileRepository)

    io.mockk.verify {
      profile.id
    }
    confirmVerified(profile)
  }

  @Test
  fun `should fail when trying to update profile without id`() {
    // given
    val profile = mockk<AuthProfileItem>()
    every { profile.id } returns null

    // when
    val testObserver = profileManager.update(profile).test()

    // then
    testObserver.assertFailure(IllegalArgumentException::class.java)

    verifyNoInteractions(profileRepository)

    io.mockk.verify {
      profile.id
    }
    confirmVerified(profile)
  }

  @Test
  fun `should read all profiles from repository`() {
    // given
    val profile = mockk<AuthProfileItem>()
    whenever(profileRepository.allProfiles).thenReturn(listOf(profile))

    // when
    val testObserver = profileManager.getAllProfiles().test()

    // then
    testObserver.assertValue(listOf(profile))

    verify(profileRepository).allProfiles
    verifyNoMoreInteractions(profileRepository)
  }

  @Test
  fun `should get currently active profile`() {
    // given
    val profiles = listOf(
      mockk<AuthProfileItem>().apply { every { isActive } returns false },
      mockk<AuthProfileItem>().apply { every { isActive } returns false },
      mockk<AuthProfileItem>().apply { every { isActive } returns true }
    )
    whenever(profileRepository.allProfiles).thenReturn(profiles)

    // when
    val testObserver = profileManager.getCurrentProfile().test()

    // then
    testObserver.assertValue(profiles.last())

    verify(profileRepository).allProfiles
    verifyNoMoreInteractions(profileRepository)
  }
}
