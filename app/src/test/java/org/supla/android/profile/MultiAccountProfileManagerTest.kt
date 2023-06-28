package org.supla.android.profile

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.supla.android.core.SuplaAppApi
import org.supla.android.core.SuplaAppProvider
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.data.source.ProfileRepository
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.DbHelper
import org.supla.android.events.ListsEventsManager
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.widget.WidgetVisibilityHandler

@RunWith(MockitoJUnitRunner::class)
class MultiAccountProfileManagerTest {

  @Mock
  private lateinit var dbHelper: DbHelper

  @Mock
  private lateinit var profileRepository: ProfileRepository

  @Mock
  private lateinit var widgetVisibilityHandler: WidgetVisibilityHandler

  @Mock
  private lateinit var listsEventsManager: ListsEventsManager

  @Mock
  private lateinit var profileIdHolder: ProfileIdHolder

  @Mock
  private lateinit var suplaAppProvider: SuplaAppProvider

  @Mock
  private lateinit var singleCallProvider: SingleCall.Provider

  private lateinit var profileManager: MultiAccountProfileManager

  @Before
  fun setUp() {
    profileManager = MultiAccountProfileManager(
      dbHelper,
      profileRepository,
      profileIdHolder,
      widgetVisibilityHandler,
      listsEventsManager,
      suplaAppProvider,
      singleCallProvider
    )
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

    verifyZeroInteractions(profileRepository)

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

    verifyZeroInteractions(profileRepository)

    io.mockk.verify {
      profile.id
    }
    confirmVerified(profile)
  }

  @Test
  fun `should delete profile`() {
    // given
    val profileId = 123L

    // when
    val testObserver = profileManager.delete(profileId).test()

    // then
    testObserver.assertComplete()

    verify(profileRepository).deleteProfile(profileId)
    verify(widgetVisibilityHandler).onProfileRemoved(profileId)
    verifyNoMoreInteractions(profileRepository, widgetVisibilityHandler)
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

  @Test
  fun `should skip activation when profile active and force is false`() {
    // given
    val activeProfileId = 123L
    val profiles = listOf(
      mockk<AuthProfileItem>().apply { every { isActive } returns false },
      mockk<AuthProfileItem>().apply { every { isActive } returns false },
      mockk<AuthProfileItem>().apply {
        every { id } returns activeProfileId
        every { isActive } returns true
      }
    )
    whenever(profileRepository.allProfiles).thenReturn(profiles)

    // when
    val testObserver = profileManager.activateProfile(activeProfileId, false).test()

    // then
    testObserver.assertComplete()

    verify(profileRepository).allProfiles
    verifyNoMoreInteractions(profileRepository)
    verifyZeroInteractions(profileIdHolder, dbHelper, listsEventsManager, suplaAppProvider)
  }

  @Test
  fun `should activate other profile`() {
    // given
    val activeProfileId = 123L
    val newActiveProfileId = 234L
    val profiles = listOf(
      mockk<AuthProfileItem>().apply { every { isActive } returns false },
      mockk<AuthProfileItem>().apply {
        every { id } returns newActiveProfileId
        every { isActive } returns false
      },
      mockk<AuthProfileItem>().apply {
        every { id } returns activeProfileId
        every { isActive } returns true
      }
    )
    whenever(profileRepository.allProfiles).thenReturn(profiles)
    whenever(profileRepository.setProfileActive(newActiveProfileId)).thenReturn(true)

    val suplaClient = mockk<SuplaClientApi>()
    every { suplaClient.reconnect() } answers { }

    val suplaApp = mockk<SuplaAppApi>()
    every { suplaApp.CancelAllRestApiClientTasks(true) } answers { }
    every { suplaApp.cleanupToken() } answers { }
    every { suplaApp.getSuplaClient() } returns suplaClient
    whenever(suplaAppProvider.provide()).thenReturn(suplaApp)

    // when
    val testObserver = profileManager.activateProfile(newActiveProfileId, false).test()

    // then
    testObserver.assertComplete()

    verify(profileRepository).allProfiles
    verify(profileRepository).setProfileActive(newActiveProfileId)
    verify(profileIdHolder).profileId = newActiveProfileId
    verify(dbHelper).loadUserIconsIntoCache()
    verify(listsEventsManager).cleanup()
    verify(listsEventsManager).emitSceneUpdate()
    verify(listsEventsManager).emitGroupUpdate()
    verify(listsEventsManager).emitChannelUpdate()
    verify(suplaAppProvider).provide()
    verifyNoMoreInteractions(profileRepository, profileIdHolder, dbHelper, listsEventsManager, suplaAppProvider)

    io.mockk.verify {
      suplaApp.CancelAllRestApiClientTasks(true)
      suplaApp.cleanupToken()
      suplaApp.getSuplaClient()
      suplaClient.reconnect()
    }
    confirmVerified(suplaApp, suplaClient)
  }

  @Test
  fun `should reactivate same profile with force`() {
    // given
    val activeProfileId = 123L
    val profiles = listOf(
      mockk<AuthProfileItem>().apply { every { isActive } returns false },
      mockk<AuthProfileItem>().apply {
        every { id } returns activeProfileId
        every { isActive } returns true
      }
    )
    whenever(profileRepository.allProfiles).thenReturn(profiles)
    whenever(profileRepository.setProfileActive(activeProfileId)).thenReturn(true)

    val suplaClient = mockk<SuplaClientApi>()
    every { suplaClient.reconnect() } answers { }

    val suplaApp = mockk<SuplaAppApi>()
    every { suplaApp.CancelAllRestApiClientTasks(true) } answers { }
    every { suplaApp.cleanupToken() } answers { }
    every { suplaApp.getSuplaClient() } returns suplaClient
    whenever(suplaAppProvider.provide()).thenReturn(suplaApp)

    // when
    val testObserver = profileManager.activateProfile(activeProfileId, true).test()

    // then
    testObserver.assertComplete()

    verify(profileRepository).allProfiles
    verify(profileRepository).setProfileActive(activeProfileId)
    verify(profileIdHolder).profileId = activeProfileId
    verify(dbHelper).loadUserIconsIntoCache()
    verify(listsEventsManager).cleanup()
    verify(listsEventsManager).emitSceneUpdate()
    verify(listsEventsManager).emitGroupUpdate()
    verify(listsEventsManager).emitChannelUpdate()
    verify(suplaAppProvider).provide()
    verifyNoMoreInteractions(profileRepository, profileIdHolder, dbHelper, listsEventsManager, suplaAppProvider)

    io.mockk.verify {
      suplaApp.CancelAllRestApiClientTasks(true)
      suplaApp.cleanupToken()
      suplaApp.getSuplaClient()
      suplaClient.reconnect()
    }
    confirmVerified(suplaApp, suplaClient)
  }
}
