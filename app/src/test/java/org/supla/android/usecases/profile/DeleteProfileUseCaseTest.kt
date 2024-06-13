package org.supla.android.usecases.profile

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.Preferences
import org.supla.android.core.SuplaAppApi
import org.supla.android.core.SuplaAppProvider
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.db.AuthProfileItem
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.profile.ProfileManager
import org.supla.android.usecases.client.DisconnectUseCase

@RunWith(MockitoJUnitRunner::class)
class DeleteProfileUseCaseTest {
  @MockK
  private lateinit var context: Context

  @MockK
  private lateinit var profileManager: ProfileManager

  @MockK
  private lateinit var suplaAppProvider: SuplaAppProvider

  @MockK
  private lateinit var preferences: Preferences

  @MockK
  private lateinit var profileIdHolder: ProfileIdHolder

  @MockK
  private lateinit var activateProfileUseCase: ActivateProfileUseCase

  @MockK
  private lateinit var suplaClientStateHolder: SuplaClientStateHolder

  @MockK
  private lateinit var disconnectUseCase: DisconnectUseCase

  @InjectMockKs
  private lateinit var useCase: DeleteProfileUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should delete inactive profile`() {
    // given
    val profileId = 132L
    val profile = profileMock(profileId, false)

    every { profileManager.read(profileId) } returns Maybe.just(profile)
    every { profileManager.delete(profileId) } returns Completable.complete()

    // when
    val testObserver = useCase(profileId).test()

    // then
    testObserver.assertComplete()

    verify {
      profileManager.read(profileId)
      profileManager.delete(profileId)
    }
    confirmVerified(profileManager, preferences, profileIdHolder, context)
  }

  @Test
  fun `should delete last active profile`() {
    // given
    val profileId = 132L
    val profile = profileMock(profileId, true)

    every { profileManager.read(profileId) } returns Maybe.just(profile)
    every { profileManager.delete(profileId) } returns Completable.complete()
    every { profileManager.getAllProfiles() } returns Observable.just(emptyList())
    every { disconnectUseCase.invoke() } returns Completable.complete()
    every { preferences.isAnyAccountRegistered = false } answers {}
    every { profileIdHolder.profileId = null } answers {}
    every { suplaClientStateHolder.handleEvent(SuplaClientEvent.NoAccount) } answers {}

    // when
    val testObserver = useCase(profileId).test()

    // then
    testObserver.assertComplete()

    verify {
      profileManager.read(profileId)
      profileManager.delete(profileId)
      profileManager.getAllProfiles()
      preferences.isAnyAccountRegistered = false
      profileIdHolder.profileId = null
      disconnectUseCase.invoke()
      suplaClientStateHolder.handleEvent(SuplaClientEvent.NoAccount)
    }
    confirmVerified(profileManager, preferences, profileIdHolder, context, disconnectUseCase, suplaClientStateHolder)
  }

  @Test
  fun `should delete active profile and activate other one`() {
    // given
    val profileId = 132L
    val profileIdToActivate = 234L
    val profile = profileMock(profileId, true)

    every { activateProfileUseCase.invoke(profileIdToActivate, true) } returns Completable.complete()
    every { profileManager.read(profileId) } returns Maybe.just(profile)
    every { profileManager.delete(profileId) } returns Completable.complete()
    every { profileManager.getAllProfiles() } returns Observable.just(listOf(profileMock(profileIdToActivate, false)))
    every { disconnectUseCase.invoke() } returns Completable.complete()

    val suplaApp = mockk<SuplaAppApi>()
    every { suplaApp.SuplaClientInitIfNeed(any()) } returns null
    every { suplaAppProvider.provide() } returns suplaApp

    // when
    val testObserver = useCase(profileId).test()

    // then
    testObserver.assertComplete()

    verify {
      profileManager.read(profileId)
      profileManager.delete(profileId)
      profileManager.getAllProfiles()
      activateProfileUseCase.invoke(profileIdToActivate, true)
      suplaAppProvider.provide()
      suplaApp.SuplaClientInitIfNeed(context)
      disconnectUseCase.invoke()
    }
    confirmVerified(suplaApp, profileManager, suplaAppProvider, preferences, profileIdHolder, context, disconnectUseCase)
  }

  private fun profileMock(profileId: Long, isActive: Boolean): AuthProfileItem {
    val profile = mockk<AuthProfileItem>()
    every { profile.id } returns profileId
    every { profile.isActive } returns isActive

    return profile
  }
}
